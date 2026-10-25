package com.sara.booktracker.singleton;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Singleton: ONE activity log for the whole application.
 * Every class that writes to it, and every class that reads it, uses the same instance.
 */
public final class ActivityLog {

    private static final int MAX_EVENTS = 20;

    // 2. The single instance, created once when the class is first loaded
    private static final ActivityLog INSTANCE = new ActivityLog();

    private final Deque<String> events = new ArrayDeque<>();

    // 1. Private constructor: nobody outside can call "new ActivityLog()"
    private ActivityLog() {
    }

    // 3. The only way to get the instance
    public static ActivityLog getInstance() {
        return INSTANCE;
    }

    /** Adds an event at the top, and keeps only the latest 20. */
    public synchronized void record(String message) {
        events.addFirst(LocalDateTime.now().withNano(0) + " - " + message);
        if (events.size() > MAX_EVENTS) {
            events.removeLast();
        }
    }

    /** A copy of the events, newest first. */
    public synchronized List<String> recentEvents() {
        return List.copyOf(events);
    }
}