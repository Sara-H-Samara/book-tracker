package com.sara.booktracker.controller;

import com.sara.booktracker.dto.ActivityEventResponse;
import com.sara.booktracker.dto.ActivityResponse;
import com.sara.booktracker.dto.ApiResponse;
import com.sara.booktracker.singleton.ActivityLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    /** GET /api/activity → the latest activity, newest first */
    @GetMapping
    public ApiResponse<ActivityResponse> recentActivity() {
        List<ActivityEventResponse> events = ActivityLog.getInstance().recentEvents().stream()
                .map(event -> new ActivityEventResponse(event.timestamp(), event.message()))
                .toList();
        return ApiResponse.ok("Recent activity retrieved", new ActivityResponse(events));
    }
}
