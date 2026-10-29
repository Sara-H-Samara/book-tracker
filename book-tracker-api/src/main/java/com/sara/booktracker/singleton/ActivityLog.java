package com.sara.booktracker.singleton;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Singleton: ONE activity log for the whole application.
 * Every class that writes to it, and every class that reads it, uses the same instance.
 */
public final class ActivityLog {

    private static final int MAX_EVENTS = 20;

    /** One thing that happened, and when (UTC). */
    public record Event(Instant timestamp, String message) {
    }

    // 2. The single instance, created once when the class is first loaded
    private static final ActivityLog INSTANCE = new ActivityLog();

    private final Deque<Event> events = new ArrayDeque<>();

    // 1. Private constructor: nobody outside can call "new ActivityLog()"
    private ActivityLog() {
    }

    // 3. The only way to get the instance
    public static ActivityLog getInstance() {
        return INSTANCE;
    }

    /** Adds an event at the top, and keeps only the latest 20. */
    public synchronized void record(String message) {
        events.addFirst(new Event(Instant.now().truncatedTo(ChronoUnit.SECONDS), message));
        if (events.size() > MAX_EVENTS) {
            events.removeLast();
        }
    }

    /** A copy of the events, newest first. */
    public synchronized List<Event> recentEvents() {
        return List.copyOf(events);
    }
}
