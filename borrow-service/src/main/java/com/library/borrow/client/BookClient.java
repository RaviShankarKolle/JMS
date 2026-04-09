package com.library.borrow.client;

import com.library.borrow.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "book-service")
public interface BookClient {

    @PostMapping("/api/v1/books/{bookId}/reserve")
    ApiResponse<Object> reserve(@PathVariable("bookId") Long bookId);

    @PostMapping("/api/v1/books/{bookId}/issue")
    ApiResponse<Object> issue(@PathVariable("bookId") Long bookId);

    @PostMapping("/api/v1/books/{bookId}/return")
    ApiResponse<Object> returnBook(@PathVariable("bookId") Long bookId);
}
