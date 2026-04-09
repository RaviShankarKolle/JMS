package com.library.books.book;

import com.library.books.common.ApiResponse;
import com.library.books.exception.ApplicationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.library.books.book.BookDtos.*;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> add(
            @Valid @RequestBody CreateBookRequest request,
            @RequestHeader(value = "X-Auth-Roles", required = false) String rolesHeader
    ) {
        assertLibrarianRole(rolesHeader);
        return ResponseEntity.ok(ApiResponse.success(bookService.addBook(request), Map.of("message", "Book created.")));
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> update(
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateBookRequest request,
            @RequestHeader(value = "X-Auth-Roles", required = false) String rolesHeader
    ) {
        assertLibrarianRole(rolesHeader);
        return ResponseEntity.ok(ApiResponse.success(bookService.updateBook(bookId, request), Map.of("message", "Book updated.")));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable Long bookId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String rolesHeader
    ) {
        assertLibrarianRole(rolesHeader);
        bookService.removeBook(bookId);
        return ResponseEntity.ok(ApiResponse.success(null, Map.of("message", "Book removed.")));
    }

    @PostMapping("/{bookId}/reserve")
    public ResponseEntity<ApiResponse<BookResponse>> reserve(@PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.success(bookService.reserve(bookId), Map.of("message", "Book reserved.")));
    }

    @PostMapping("/{bookId}/issue")
    public ResponseEntity<ApiResponse<BookResponse>> issue(@PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.success(bookService.issue(bookId), Map.of("message", "Book issued.")));
    }

    @PostMapping("/{bookId}/return")
    public ResponseEntity<ApiResponse<BookResponse>> returnBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.success(bookService.returnBook(bookId), Map.of("message", "Book returned.")));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BookPageResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(bookService.list(page, size), Map.of()));
    }

    private void assertLibrarianRole(String rolesHeader) {
        if (rolesHeader == null) {
            throw new ApplicationException("ACCESS_DENIED", "Only LIBRARIAN or ADMIN can modify books.", HttpStatus.FORBIDDEN);
        }
        String upper = rolesHeader.toUpperCase();
        if (!upper.contains("LIBRARIAN") && !upper.contains("ADMIN")) {
            throw new ApplicationException("ACCESS_DENIED", "Only LIBRARIAN or ADMIN can modify books.", HttpStatus.FORBIDDEN);
        }
    }
}
