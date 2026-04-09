package com.library.books.book;

import com.library.books.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.library.books.book.BookDtos.*;

@Service
public class BookService {
    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private static final long CACHE_TTL_SECONDS = 60L;

    private final BookRepository bookRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, CacheEntry> catalogCache = new ConcurrentHashMap<>();

    public BookService(BookRepository bookRepository, ApplicationEventPublisher eventPublisher) {
        this.bookRepository = bookRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BookResponse addBook(CreateBookRequest request) {
        if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new ApplicationException("BOOK_EXISTS", "ISBN already exists.", HttpStatus.CONFLICT);
        }
        Long id = bookRepository.create(
                request.title(), request.author(), request.genre(), request.isbn(), request.totalCopies(), request.description()
        );
        BookResponse response = map(bookRepository.findById(id).orElseThrow());
        invalidateCatalogCache();
        publishBookEvent("BOOK_ADDED", response.id(), response.isbn());
        return response;
    }

    @Transactional
    public BookResponse updateBook(Long bookId, UpdateBookRequest request) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new ApplicationException("BOOK_NOT_FOUND", "Book not found.", HttpStatus.NOT_FOUND));
        bookRepository.update(bookId, request.title(), request.author(), request.genre(), request.isbn(), request.totalCopies(), request.description());
        BookResponse response = map(bookRepository.findById(bookId).orElseThrow());
        invalidateCatalogCache();
        publishBookEvent("BOOK_UPDATED", response.id(), response.isbn());
        return response;
    }

    @Transactional
    public void removeBook(Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new ApplicationException("BOOK_NOT_FOUND", "Book not found.", HttpStatus.NOT_FOUND));
        bookRepository.markRemoved(bookId);
        invalidateCatalogCache();
        publishBookEvent("BOOK_REMOVED", bookId, null);
    }

    @Transactional
    public BookResponse reserve(Long bookId) {
        int updated = bookRepository.reserveCopy(bookId);
        if (updated == 0) {
            throw new ApplicationException("BOOK_NOT_AVAILABLE", "Book is unavailable for reservation.", HttpStatus.CONFLICT);
        }
        return map(bookRepository.findById(bookId).orElseThrow());
    }

    @Transactional
    public BookResponse issue(Long bookId) {
        int updated = bookRepository.issueCopy(bookId);
        if (updated == 0) {
            throw new ApplicationException("BOOK_NOT_FOUND", "Book not found.", HttpStatus.NOT_FOUND);
        }
        return map(bookRepository.findById(bookId).orElseThrow());
    }

    @Transactional
    public BookResponse returnBook(Long bookId) {
        int updated = bookRepository.returnCopy(bookId);
        if (updated == 0) {
            throw new ApplicationException("BOOK_NOT_FOUND", "Book not found.", HttpStatus.NOT_FOUND);
        }
        return map(bookRepository.findById(bookId).orElseThrow());
    }

    public BookPageResponse list(int page, int size) {
        int validPage = Math.max(page, 0);
        int validSize = Math.min(Math.max(size, 1), 100);
        String cacheKey = validPage + ":" + validSize;
        CacheEntry cached = catalogCache.get(cacheKey);
        if (cached != null && cached.expiresAt.isAfter(Instant.now())) {
            return cached.data;
        }
        int offset = validPage * validSize;
        List<BookResponse> items = bookRepository.list(offset, validSize).stream().map(this::map).toList();
        BookPageResponse response = new BookPageResponse(items, validPage, validSize, bookRepository.countActive());
        catalogCache.put(cacheKey, new CacheEntry(response, Instant.now().plusSeconds(CACHE_TTL_SECONDS)));
        return response;
    }

    private BookResponse map(BookRepository.BookRecord r) {
        return new BookResponse(
                r.id(), r.title(), r.author(), r.genre(), r.isbn(), r.totalCopies(),
                r.availableCopies(), r.status(), r.description(), r.updatedAt()
        );
    }

    private void invalidateCatalogCache() {
        catalogCache.clear();
    }

    private void publishBookEvent(String type, Long bookId, String isbn) {
        eventPublisher.publishEvent(new BookCatalogEvent(type, bookId, isbn, Instant.now()));
        log.info("Book event emitted: type={}, bookId={}, isbn={}", type, bookId, isbn);
    }

    private record CacheEntry(BookPageResponse data, Instant expiresAt) {}

    public record BookCatalogEvent(String type, Long bookId, String isbn, Instant occurredAt) {}
}
