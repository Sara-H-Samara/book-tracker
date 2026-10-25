package com.sara.booktracker.dto;

import com.sara.booktracker.entity.ReadingStatus;

import java.time.LocalDate;

public record ShelfEntryResponse(
        Long id,
        Long bookId,
        String bookTitle,
        ReadingStatus status,
        Integer currentPage,
        Integer totalPages,
        LocalDate startedAt,
        LocalDate finishedAt
) {
}