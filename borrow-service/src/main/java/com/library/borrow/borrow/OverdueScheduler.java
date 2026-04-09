package com.library.borrow.borrow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueScheduler {
    private static final Logger log = LoggerFactory.getLogger(OverdueScheduler.class);
    private final BorrowService borrowService;

    public OverdueScheduler(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    // Runs every day at 1:00 AM server time.
    @Scheduled(cron = "0 0 1 * * *")
    public void processOverdues() {
        int processed = borrowService.processOverdueRecords();
        log.info("Overdue scheduler run complete. Processed records={}", processed);
    }
}
