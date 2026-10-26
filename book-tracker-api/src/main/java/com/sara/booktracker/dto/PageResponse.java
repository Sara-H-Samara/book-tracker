package com.sara.booktracker.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A stable, simple JSON shape for any paged list (books, genres, ...).
 * <T> means "any type": PageResponse<BookResponse>, PageResponse<GenreResponse>, ...
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}