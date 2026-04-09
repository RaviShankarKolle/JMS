package com.library.books.book;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BookEventListener {
    private static final Logger log = LoggerFactory.getLogger(BookEventListener.class);

    @Async
    @EventListener
    public void onBookEvent(BookService.BookCatalogEvent event) {
        log.info("Async book catalog event handled: type={}, bookId={}, isbn={}, at={}",
                event.type(), event.bookId(), event.isbn(), event.occurredAt());
    }
}
