package com.sara.booktracker.dto;

import java.util.Set;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        Integer pages,
        Integer publishedYear,
        Long authorId,
        String authorName,
        Set<String> genres
) {
}