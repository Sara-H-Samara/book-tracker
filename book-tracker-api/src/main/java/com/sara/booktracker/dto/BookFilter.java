package com.sara.booktracker.dto;

import jakarta.validation.constraints.AssertTrue;
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

    /**
     * Cross-field rule: if both are sent, minPages can't be bigger than maxPages.
     */
    @AssertTrue(message = "minPages must be less than or equal to maxPages")
    public boolean isPageRangeValid() {
        return minPages == null || maxPages == null || minPages <= maxPages;
    }

}