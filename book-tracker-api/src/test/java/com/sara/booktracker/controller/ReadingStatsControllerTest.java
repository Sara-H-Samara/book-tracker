package com.sara.booktracker.controller;

import com.sara.booktracker.MockSecurityBeans;
import com.sara.booktracker.dto.ReadingStatsResponse;
import com.sara.booktracker.service.ReadingStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for GET /api/users/{userId}/stats: only the controller, validation and
 * GlobalExceptionHandler are started. ReadingStatsService is a mock.
 */
@WebMvcTest(ReadingStatsController.class)
@MockSecurityBeans
class ReadingStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReadingStatsService readingStatsService;

    @BeforeEach
    void stubGetStats() {
        when(readingStatsService.getStats(eq(1L), any()))
                .thenReturn(new ReadingStatsResponse(1L, 2026, 0, 0, List.of()));
    }

    @Test
    void yearAboveMax_returns400() throws Exception {
        mockMvc.perform(get("/api/users/1/stats").param("year", "2101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("year"))
                .andExpect(jsonPath("$.errors[0].message").value("year must be 2100 or earlier"));

        verifyNoInteractions(readingStatsService);
    }

    @Test
    void yearBelowMin_returns400() throws Exception {
        mockMvc.perform(get("/api/users/1/stats").param("year", "1899"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("year"))
                .andExpect(jsonPath("$.errors[0].message").value("year must be 1900 or later"));

        verifyNoInteractions(readingStatsService);
    }

    /** Before the fix, this year reached LocalDate.of(...) and caused a 500. */
    @Test
    void yearThatUsedToCrash_returns400Not500() throws Exception {
        mockMvc.perform(get("/api/users/1/stats").param("year", "1000000000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));

        verifyNoInteractions(readingStatsService);
    }

    @ParameterizedTest
    @ValueSource(ints = {1900, 2100})
    void yearAtBoundary_returns200(int year) throws Exception {
        mockMvc.perform(get("/api/users/1/stats").param("year", String.valueOf(year)))
                .andExpect(status().isOk());

        verify(readingStatsService).getStats(1L, year);
    }

    @Test
    void noYear_returns200AndPassesNullToService() throws Exception {
        mockMvc.perform(get("/api/users/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Reading statistics retrieved"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.finishedByGenre").isArray());

        verify(readingStatsService).getStats(1L, null);
    }
}
