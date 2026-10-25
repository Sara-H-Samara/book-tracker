package com.sara.booktracker.dto;

import com.sara.booktracker.entity.ReadingStatus;
import jakarta.validation.constraints.NotNull;

/**
 * userId is sent in the body only until JWT auth exists.
 * Later it will come from the logged-in user instead.
 */
public record AddToShelfRequest(

        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "bookId is required")
        Long bookId,

        ReadingStatus status   // optional, defaults to WANT_TO_READ
) {
}