package com.sara.booktracker.controller;

import com.sara.booktracker.dto.ReadingStatsResponse;
import com.sara.booktracker.service.ReadingStatsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/stats")
public class ReadingStatsController {

    private final ReadingStatsService readingStatsService;

    /** GET /api/users/1/stats?year=2026 → 200, 400 if the year is out of range, 404 if the user doesn't exist */
    @GetMapping
    public ReadingStatsResponse getStats(
            @PathVariable Long userId,
            @RequestParam(required = false)
            @Min(value = 1900, message = "year must be 1900 or later")
            @Max(value = 2100, message = "year must be 2100 or earlier")
            Integer year) {
        return readingStatsService.getStats(userId, year);
    }
}