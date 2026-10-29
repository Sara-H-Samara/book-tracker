package com.sara.booktracker.dto;

import java.time.Instant;

/** One activity event. timestamp is UTC, e.g. "2026-10-28T09:15:02Z". */
public record ActivityEventResponse(Instant timestamp, String message) {
}
