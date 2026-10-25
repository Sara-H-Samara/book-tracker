package com.sara.booktracker.factory;

import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.ReadingStatus;
import com.sara.booktracker.entity.ShelfEntry;
import com.sara.booktracker.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class ShelfEntryFactoryTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-10-21T10:00:00Z"), ZoneOffset.UTC);
    private final LocalDate today = LocalDate.of(2026, 10, 21);
    private final ShelfEntryFactory factory = new ShelfEntryFactory(fixedClock);

    private final User user = new User();
    private final Book book = bookWithPages(688);

    @Test
    void wantToRead_hasNoDatesAndStartsAtPageZero() {
        ShelfEntry entry = factory.create(user, book, ReadingStatus.WANT_TO_READ);

        assertEquals(ReadingStatus.WANT_TO_READ, entry.getStatus());
        assertEquals(0, entry.getCurrentPage());
        assertNull(entry.getStartedAt());
        assertNull(entry.getFinishedAt());
    }

    @Test
    void reading_setsStartedAtToToday() {
        ShelfEntry entry = factory.create(user, book, ReadingStatus.READING);

        assertEquals(today, entry.getStartedAt());
        assertNull(entry.getFinishedAt());
        assertEquals(0, entry.getCurrentPage());
    }

    @Test
    void finished_setsFinishedAtAndLastPage() {
        ShelfEntry entry = factory.create(user, book, ReadingStatus.FINISHED);

        assertEquals(today, entry.getFinishedAt());
        assertEquals(688, entry.getCurrentPage());
    }

    @Test
    void nullStatus_defaultsToWantToRead() {
        ShelfEntry entry = factory.create(user, book, null);

        assertEquals(ReadingStatus.WANT_TO_READ, entry.getStatus());
    }

    @Test
    void entryIsLinkedToUserAndBook() {
        ShelfEntry entry = factory.create(user, book, ReadingStatus.READING);

        assertSame(user, entry.getUser());
        assertSame(book, entry.getBook());
    }

    private static Book bookWithPages(int pages) {
        Book b = new Book();
        b.setTitle("Dune");
        b.setPages(pages);
        return b;
    }
}