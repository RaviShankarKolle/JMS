package com.library.borrow.borrow;

import com.library.borrow.common.ApiResponse;
import com.library.borrow.exception.ApplicationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.library.borrow.borrow.BorrowDtos.*;

@RestController
@RequestMapping("/api/v1/borrows")
public class BorrowController {
    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<ApiResponse<BorrowResponse>>> create(
            @Valid @RequestBody BorrowRequest request,
            @RequestHeader(value = "X-Auth-Roles", required = false) String rolesHeader
    ) {
        assertUserRole(rolesHeader);
        return borrowService.createBorrowAsync(request)
                .thenApply(response -> ResponseEntity.accepted()
                        .body(ApiResponse.success(response, Map.of("message", "Borrow request accepted."))));
    }

    @PostMapping("/{borrowId}/allocate")
    public ResponseEntity<ApiResponse<BorrowResponse>> allocate(
            @PathVariable Long borrowId,
            @Valid @RequestBody AllocateRequest request,
            @RequestHeader(value = "X-Auth-Roles", required = false) String rolesHeader
    ) {
        assertLibrarianRole(rolesHeader);
        return ResponseEntity.ok(ApiResponse.success(borrowService.allocate(borrowId, request), Map.of("message", "Borrow allocated.")));
    }

    @PostMapping("/{borrowId}/return")
    public CompletableFuture<ResponseEntity<ApiResponse<BorrowResponse>>> returnBook(
            @PathVariable Long borrowId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String rolesHeader
    ) {
        assertLibrarianRole(rolesHeader);
        return borrowService.returnBookAsync(borrowId)
                .thenApply(response -> ResponseEntity.accepted()
                        .body(ApiResponse.success(response, Map.of("message", "Borrow return accepted."))));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<BorrowListResponse>> listByUser(
            @PathVariable Long userId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String rolesHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        assertUserRole(rolesHeader);
        return ResponseEntity.ok(ApiResponse.success(borrowService.listByUser(userId, page, size), Map.of()));
    }

    private void assertUserRole(String rolesHeader) {
        if (rolesHeader == null || !rolesHeader.toUpperCase().contains("USER")) {
            throw new ApplicationException("ACCESS_DENIED", "Only USER role can borrow or view borrow list.", HttpStatus.FORBIDDEN);
        }
    }

    private void assertLibrarianRole(String rolesHeader) {
        if (rolesHeader == null) {
            throw new ApplicationException("ACCESS_DENIED", "Only LIBRARIAN or ADMIN can allocate/return books.", HttpStatus.FORBIDDEN);
        }
        String upper = rolesHeader.toUpperCase();
        if (!upper.contains("LIBRARIAN") && !upper.contains("ADMIN")) {
            throw new ApplicationException("ACCESS_DENIED", "Only LIBRARIAN or ADMIN can allocate/return books.", HttpStatus.FORBIDDEN);
        }
    }
}
