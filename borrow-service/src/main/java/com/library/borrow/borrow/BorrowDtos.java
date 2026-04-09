package com.library.borrow.borrow;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class BorrowDtos {

    public record BorrowRequest(
            @NotNull @Positive Long userId,
            @NotNull @Positive Long bookId,
            @NotNull LocalDate startDate,
            @NotNull @Future LocalDate endDate
    ) {}

    public record AllocateRequest(@NotNull LocalDate dueDate) {}

    public record BorrowResponse(
            Long id,
            Long userId,
            Long bookId,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate dueDate,
            String status,
            Instant updatedAt
    ) {}

    public record BorrowListResponse(List<BorrowResponse> items, int page, int size, long total) {}
}
