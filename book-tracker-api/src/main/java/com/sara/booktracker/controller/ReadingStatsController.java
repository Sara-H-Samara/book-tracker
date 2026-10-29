package com.sara.booktracker.controller;

import com.sara.booktracker.dto.ApiResponse;
import com.sara.booktracker.dto.ReadingStatsResponse;
import com.sara.booktracker.service.ReadingStatsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class ReadingStatsController {

    private final ReadingStatsService readingStatsService;

    /** GET /api/users/me/stats → the logged-in user's own stats */
    @GetMapping("/me/stats")
    public ApiResponse<ReadingStatsResponse> getMyStats(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(required = false)
            @Min(value = 1900, message = "year must be 1900 or later")
            @Max(value = 2100, message = "year must be 2100 or earlier") Integer year) {
        return ApiResponse.ok("Reading statistics retrieved",
                readingStatsService.getStatsForUsername(currentUser.getUsername(), year));
    }

    /** GET /api/users/{userId}/stats → any user's stats, ADMIN only */
    @GetMapping("/{userId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReadingStatsResponse> getStats(
            @PathVariable Long userId,
            @RequestParam(required = false)
            @Min(value = 1900, message = "year must be 1900 or later")
            @Max(value = 2100, message = "year must be 2100 or earlier") Integer year) {
        return ApiResponse.ok("Reading statistics retrieved", readingStatsService.getStats(userId, year));
    }
}