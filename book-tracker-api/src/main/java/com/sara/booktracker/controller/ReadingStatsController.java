package com.sara.booktracker.controller;

import com.sara.booktracker.dto.ReadingStatsResponse;
import com.sara.booktracker.service.ReadingStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/stats")
public class ReadingStatsController {

    private final ReadingStatsService readingStatsService;

    /** GET /api/users/1/stats?year=2026 → 200, or 404 if the user doesn't exist */
    @GetMapping
    public ReadingStatsResponse getStats(@PathVariable Long userId,
                                         @RequestParam(required = false) Integer year) {
        return readingStatsService.getStats(userId, year);
    }
}