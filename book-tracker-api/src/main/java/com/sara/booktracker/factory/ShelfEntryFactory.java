package com.sara.booktracker.factory;

import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.ReadingStatus;
import com.sara.booktracker.entity.ShelfEntry;
import com.sara.booktracker.entity.User;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Factory pattern (Simple Factory, creational).
 *
 * The ONLY place that knows how to build a valid ShelfEntry.
 * Callers say WHAT they want (user, book, status); the factory decides HOW
 * to set up the object so it follows the business rules:
 *   - WANT_TO_READ → page 0, no dates
 *   - READING      → page 0, startedAt = today
 *   - FINISHED     → page = last page, finishedAt = today
 */
@Component
public class ShelfEntryFactory {

    private final Clock clock;

    public ShelfEntryFactory(Clock clock) {
        this.clock = clock;
    }

    public ShelfEntry create(User user, Book book, ReadingStatus status) {
        ReadingStatus initialStatus = (status != null) ? status : ReadingStatus.WANT_TO_READ;
        LocalDate today = LocalDate.now(clock);

        ShelfEntry entry = new ShelfEntry();
        entry.setUser(user);
        entry.setBook(book);
        entry.setStatus(initialStatus);

        switch (initialStatus) {
            case WANT_TO_READ -> entry.setCurrentPage(0);
            case READING -> {
                entry.setCurrentPage(0);
                entry.setStartedAt(today);
            }
            case FINISHED -> {
                entry.setCurrentPage(book.getPages() != null ? book.getPages() : 0);
                entry.setFinishedAt(today); // start date unknown, so startedAt stays null
            }
        }
        return entry;
    }
}