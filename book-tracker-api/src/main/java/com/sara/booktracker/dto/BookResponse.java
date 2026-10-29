package com.sara.booktracker.dto;

import java.util.List;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        Integer pages,
        Integer publishedYear,
        AuthorSummary author,        // null when the book has no author
        List<GenreSummary> genres    // sorted by name
) {
}
