package com.sara.booktracker.controller;

import com.sara.booktracker.MockSecurityBeans;
import com.sara.booktracker.singleton.ActivityLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for GET /api/activity. ActivityLog is a real singleton shared by the whole
 * JVM, so each test writes its own unique messages and only checks the newest events.
 */
@WebMvcTest(ActivityController.class)
@MockSecurityBeans
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void events_areTypedObjectsWithUtcTimestampAndMessage() throws Exception {
        String message = "Book created: " + UUID.randomUUID();
        ActivityLog.getInstance().record(message);

        mockMvc.perform(get("/api/activity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events[0].message").value(message))
                .andExpect(jsonPath("$.data.events[0].timestamp",
                        matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")))
                .andExpect(jsonPath("$.data.events[0].length()").value(2));   // only timestamp + message
    }

    @Test
    void response_hasNoInternalDetails() throws Exception {
        mockMvc.perform(get("/api/activity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Recent activity retrieved"))
                .andExpect(jsonPath("$.data.instanceId").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(1));             // only "events"
    }

    @Test
    void events_areNewestFirstAndCappedAt20() throws Exception {
        String run = UUID.randomUUID().toString();
        for (int i = 0; i < 25; i++) {
            ActivityLog.getInstance().record(run + " event " + i);
        }

        mockMvc.perform(get("/api/activity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events.length()").value(20))
                .andExpect(jsonPath("$.data.events[0].message").value(run + " event 24"))
                .andExpect(jsonPath("$.data.events[19].message").value(run + " event 5"));
    }
}
