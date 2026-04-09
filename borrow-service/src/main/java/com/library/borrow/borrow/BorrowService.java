package com.library.borrow.borrow;

import com.library.borrow.client.BookClient;
import com.library.borrow.client.NotificationClient;
import com.library.borrow.client.UserClient;
import com.library.borrow.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.library.borrow.borrow.BorrowDtos.*;

@Service
public class BorrowService {
    private static final Logger log = LoggerFactory.getLogger(BorrowService.class);
    private static final int MAX_ACTIVE_BOOKS_PER_USER = 3;
    private static final int MAX_BORROW_DAYS = 14;
    private static final long CACHE_TTL_SECONDS = 45L;

    private final BorrowRepository borrowRepository;
    private final UserClient userClient;
    private final BookClient bookClient;
    private final NotificationClient notificationClient;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<Long, CacheEntry> userBorrowCache = new ConcurrentHashMap<>();

    public BorrowService(BorrowRepository borrowRepository,
                         UserClient userClient,
                         BookClient bookClient,
                         NotificationClient notificationClient,
                         ApplicationEventPublisher eventPublisher) {
        this.borrowRepository = borrowRepository;
        this.userClient = userClient;
        this.bookClient = bookClient;
        this.notificationClient = notificationClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BorrowResponse createBorrow(BorrowRequest request) {
        if (!request.endDate().isAfter(request.startDate())) {
            throw new ApplicationException("INVALID_BORROW_WINDOW", "End date must be after start date.", HttpStatus.BAD_REQUEST);
        }
        if (request.startDate().plusDays(MAX_BORROW_DAYS).isBefore(request.endDate())) {
            throw new ApplicationException("BORROW_DURATION_EXCEEDED", "Max borrow duration is 14 days.", HttpStatus.BAD_REQUEST);
        }
        if (borrowRepository.countActiveLoans(request.userId()) >= MAX_ACTIVE_BOOKS_PER_USER) {
            throw new ApplicationException("BORROW_LIMIT_REACHED", "Max active borrow limit reached.", HttpStatus.CONFLICT);
        }
        var eligibilityResponse = userClient.getEligibility(request.userId());
        if (eligibilityResponse.data() == null || !eligibilityResponse.data().eligible()) {
            throw new ApplicationException("USER_NOT_ELIGIBLE", "User is not eligible for borrowing.", HttpStatus.CONFLICT);
        }
        bookClient.reserve(request.bookId());
        Long id = borrowRepository.createPending(request.userId(), request.bookId(), request.startDate(), request.endDate());
        BorrowResponse response = map(borrowRepository.findById(id).orElseThrow());
        invalidateUserCache(request.userId());
        publishBorrowEvent("BORROW_CREATED", response.id(), response.userId(), response.bookId());
        return response;
    }

    @Async
    public CompletableFuture<BorrowResponse> createBorrowAsync(BorrowRequest request) {
        return CompletableFuture.completedFuture(createBorrow(request));
    }

    @Transactional
    public BorrowResponse allocate(Long borrowId, AllocateRequest request) {
        BorrowRepository.BorrowRecord record = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ApplicationException("BORROW_NOT_FOUND", "Borrow record not found.", HttpStatus.NOT_FOUND));
        if (!"PENDING_PICKUP".equalsIgnoreCase(record.status())) {
            throw new ApplicationException("INVALID_BORROW_STATE", "Only pending pickup records can be allocated.", HttpStatus.CONFLICT);
        }
        LocalDate dueDate = request.dueDate();
        if (!dueDate.isAfter(LocalDate.now())) {
            throw new ApplicationException("INVALID_DUE_DATE", "Due date must be in the future.", HttpStatus.BAD_REQUEST);
        }
        borrowRepository.markAllocated(borrowId, dueDate);
        bookClient.issue(record.bookId());
        BorrowResponse response = map(borrowRepository.findById(borrowId).orElseThrow());
        invalidateUserCache(response.userId());
        publishBorrowEvent("BORROW_ALLOCATED", response.id(), response.userId(), response.bookId());
        return response;
    }

    @Transactional
    public BorrowResponse returnBook(Long borrowId) {
        BorrowRepository.BorrowRecord record = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ApplicationException("BORROW_NOT_FOUND", "Borrow record not found.", HttpStatus.NOT_FOUND));
        if ("RETURNED".equalsIgnoreCase(record.status())) {
            throw new ApplicationException("ALREADY_RETURNED", "Book is already returned.", HttpStatus.CONFLICT);
        }
        borrowRepository.markReturned(borrowId);
        bookClient.returnBook(record.bookId());
        BorrowResponse response = map(borrowRepository.findById(borrowId).orElseThrow());
        invalidateUserCache(response.userId());
        publishBorrowEvent("BORROW_RETURNED", response.id(), response.userId(), response.bookId());
        return response;
    }

    @Async
    public CompletableFuture<BorrowResponse> returnBookAsync(Long borrowId) {
        return CompletableFuture.completedFuture(returnBook(borrowId));
    }

    public BorrowListResponse listByUser(Long userId, int page, int size) {
        CacheEntry cacheEntry = userBorrowCache.get(userId);
        if (cacheEntry != null && cacheEntry.expiresAt().isAfter(Instant.now())
                && cacheEntry.data().page() == Math.max(page, 0)
                && cacheEntry.data().size() == Math.min(Math.max(size, 1), 100)) {
            return cacheEntry.data();
        }
        int validPage = Math.max(page, 0);
        int validSize = Math.min(Math.max(size, 1), 100);
        int offset = validPage * validSize;
        List<BorrowResponse> items = borrowRepository.listByUser(userId, offset, validSize).stream().map(this::map).toList();
        BorrowListResponse response = new BorrowListResponse(items, validPage, validSize, borrowRepository.countByUser(userId));
        userBorrowCache.put(userId, new CacheEntry(response, Instant.now().plusSeconds(CACHE_TTL_SECONDS)));
        return response;
    }

    @Transactional
    public int processOverdueRecords() {
        List<BorrowRepository.BorrowRecord> overdueRecords = borrowRepository.findAllocatedOverdue(LocalDate.now());
        for (BorrowRepository.BorrowRecord record : overdueRecords) {
            borrowRepository.markOverdue(record.id());
            invalidateUserCache(record.userId());
            publishBorrowEvent("BORROW_OVERDUE", record.id(), record.userId(), record.bookId());
            try {
                notificationClient.sendOverdueNotification(new NotificationClient.OverdueNotificationRequest(
                        "user-" + record.userId() + "@example.com",
                        "User " + record.userId(),
                        record.bookId(),
                        record.id(),
                        record.dueDate()
                ));
            } catch (Exception ex) {
                log.warn("Failed to dispatch overdue notification for borrowId {}: {}", record.id(), ex.getMessage());
            }
        }
        return overdueRecords.size();
    }

    private BorrowResponse map(BorrowRepository.BorrowRecord r) {
        return new BorrowResponse(r.id(), r.userId(), r.bookId(), r.startDate(), r.endDate(), r.dueDate(), r.status(), r.updatedAt());
    }

    private void invalidateUserCache(Long userId) {
        userBorrowCache.remove(userId);
    }

    private void publishBorrowEvent(String type, Long borrowId, Long userId, Long bookId) {
        eventPublisher.publishEvent(new BorrowDomainEvent(type, borrowId, userId, bookId, Instant.now()));
        log.info("Borrow event emitted: type={}, borrowId={}, userId={}, bookId={}", type, borrowId, userId, bookId);
    }

    private record CacheEntry(BorrowListResponse data, Instant expiresAt) {}

    public record BorrowDomainEvent(String type, Long borrowId, Long userId, Long bookId, Instant occurredAt) {}
}
