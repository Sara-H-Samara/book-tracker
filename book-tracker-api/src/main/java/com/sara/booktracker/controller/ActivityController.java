package com.sara.booktracker.controller;

import com.sara.booktracker.singleton.ActivityLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    /** GET /api/activity → the latest activity, newest first */
    @GetMapping
    public Map<String, Object> recentActivity() {
        ActivityLog log = ActivityLog.getInstance();

        return Map.of(
                "instanceId", System.identityHashCode(log),
                "events", log.recentEvents()
        );
    }
}