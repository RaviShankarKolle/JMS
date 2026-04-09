package com.library.borrow.borrow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BorrowEventListener {
    private static final Logger log = LoggerFactory.getLogger(BorrowEventListener.class);

    @Async
    @EventListener
    public void onBorrowEvent(BorrowService.BorrowDomainEvent event) {
        log.info("Async borrow event handled: type={}, borrowId={}, userId={}, bookId={}, at={}",
                event.type(), event.borrowId(), event.userId(), event.bookId(), event.occurredAt());
    }
}
