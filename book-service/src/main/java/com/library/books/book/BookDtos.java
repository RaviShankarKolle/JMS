package com.library.books.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public class BookDtos {

    public record CreateBookRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 120) String author,
            @NotBlank @Size(max = 80) String genre,
            @NotBlank @Pattern(regexp = "^[0-9\\-]{10,17}$", message = "Invalid ISBN format") String isbn,
            @Min(1) int totalCopies,
            String description
    ) {}

    public record UpdateBookRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 120) String author,
            @NotBlank @Size(max = 80) String genre,
            @NotBlank @Pattern(regexp = "^[0-9\\-]{10,17}$", message = "Invalid ISBN format") String isbn,
            @Min(1) int totalCopies,
            String description
    ) {}

    public record BookResponse(
            Long id,
            String title,
            String author,
            String genre,
            String isbn,
            int totalCopies,
            int availableCopies,
            String status,
            String description,
            Instant updatedAt
    ) {}

    public record BookPageResponse(List<BookResponse> items, int page, int size, long total) {}
}
