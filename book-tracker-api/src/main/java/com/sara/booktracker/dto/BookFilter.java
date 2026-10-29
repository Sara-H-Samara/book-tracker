package com.sara.booktracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Optional filters for searching books. Every field can be null (= "don't filter by this").
 * Spring fills it from the query string: /api/books?genre=classic&minPages=400
 */
public record BookFilter(

        @Schema(description = "Books whose title contains this text. Example: dune")
        String title,

        @Schema(description = "Only books by this author id. Example: 1")
        Long authorId,

        @Schema(description = "Only books with this genre name. Example: Classic")
        String genre,

        @Schema(description = "Books with at least this many pages. Example: 100")
        @PositiveOrZero Integer minPages,

        @Schema(description = "Books with at most this many pages. Must be ≥ minPages. Example: 500")
        @PositiveOrZero Integer maxPages
) {

    /**
     * Cross-field rule: if both are sent, minPages can't be bigger than maxPages.
     */
    @Schema(hidden = true)
    @AssertTrue(message = "minPages must be less than or equal to maxPages")
    public boolean isPageRangeValid() {
        return minPages == null || maxPages == null || minPages <= maxPages;
    }
}