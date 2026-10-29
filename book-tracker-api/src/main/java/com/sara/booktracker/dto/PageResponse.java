package com.sara.booktracker.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A stable, simple JSON shape for any paged list (books, shelf entries, ...):
 * { "content": [...], "page": { "number", "size", "totalElements", "totalPages" } }
 * <T> means "any type": PageResponse<BookResponse>, PageResponse<ShelfEntryResponse>, ...
 */
public record PageResponse<T>(
        List<T> content,
        PageMetadata page
) {

    /** number is 0-based: the first page is 0. */
    public record PageMetadata(
            int number,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                new PageMetadata(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );
    }
}
