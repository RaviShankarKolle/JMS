package com.library.borrow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/v1/notifications/overdue")
    void sendOverdueNotification(@RequestBody OverdueNotificationRequest request);

    record OverdueNotificationRequest(
            String email,
            String name,
            Long bookId,
            Long borrowId,
            LocalDate dueDate
    ) {}
}
