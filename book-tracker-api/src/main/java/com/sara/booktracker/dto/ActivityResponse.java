package com.sara.booktracker.dto;

import java.util.List;

/** GET /api/activity: at most 20 events, newest first, so no pagination. */
public record ActivityResponse(List<ActivityEventResponse> events) {
}
