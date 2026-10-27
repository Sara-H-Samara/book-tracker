package com.sara.booktracker.service;

import com.sara.booktracker.dto.ReadingStatsResponse;

public interface ReadingStatsService {

    ReadingStatsResponse getStats(Long userId, Integer year);
}