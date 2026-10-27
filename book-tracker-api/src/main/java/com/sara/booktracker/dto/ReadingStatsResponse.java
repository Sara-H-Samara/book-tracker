package com.sara.booktracker.dto;

import java.util.List;

public record ReadingStatsResponse(
        Long userId,
        int year,
        long booksFinished,
        long pagesRead,
        List<GenreCount> finishedByGenre
) {
}