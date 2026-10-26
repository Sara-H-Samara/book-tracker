package com.sara.booktracker.dto;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * Optional filters for searching books. Every field can be null (= "don't filter by this").
 * Spring fills it from the query string: /api/books?genre=classic&minPages=400
 */
public record BookFilter(
        String title,
        Long authorId,
        String genre,
        @PositiveOrZero Integer minPages,
        @PositiveOrZero Integer maxPages
) {
}