package com.sara.booktracker.dto;

import com.sara.booktracker.entity.ReadingStatus;

import java.time.LocalDate;

public record ShelfEntryResponse(
        Long id,
        ReadingStatus status,
        Integer currentPage,
        LocalDate startedAt,
        LocalDate finishedAt,
        BookSummary book
) {
}
