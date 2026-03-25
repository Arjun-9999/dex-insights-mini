package com.dex.dex_insights_mini.controller;

import com.dex.dex_insights_mini.service.InsightsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InsightsController.class)
class InsightsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InsightsService insightsService;

    @Test
    void getOverview_returnsInsightsPayload() throws Exception {
        when(insightsService.generateOverview()).thenReturn(Map.of(
                "incidentCountsBySeverity", Map.of("HIGH", 2, "LOW", 1),
                "revenueByStore", Map.of("S1", 120.5)
        ));

        mockMvc.perform(get("/v1/insights/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentCountsBySeverity.HIGH").value(2))
                .andExpect(jsonPath("$.incidentCountsBySeverity.LOW").value(1))
                .andExpect(jsonPath("$.revenueByStore.S1").value(120.5));
    }
}

