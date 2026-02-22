package com.gridops.pipeline.api;

import com.gridops.pipeline.domain.TelemetryEventType;
import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.pipeline.TelemetryPipeline;
import com.gridops.pipeline.repository.TelemetryEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for {@link TelemetryController}.
 */
@WebMvcTest(TelemetryController.class)
class TelemetryControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TelemetryPipeline pipeline;

    @MockBean
    TelemetryEventRepository repository;

    @Test
    void ingestValidEvent_returns200() throws Exception {
        TelemetryPipelineContext ctx = successCtx();
        when(pipeline.process(anyString())).thenReturn(ctx);

        mvc.perform(post("/api/telemetry/ingest")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{\"type\":\"OUTAGE\",\"deviceId\":\"DEV-001\",\"timestamp\":\"2024-01-15T10:30:00Z\",\"value\":1500.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.eventType").value("OUTAGE"))
                .andExpect(jsonPath("$.persistedId").value(42));
    }

    @Test
    void ingestInvalidEvent_returns422() throws Exception {
        TelemetryPipelineContext ctx = new TelemetryPipelineContext("bad");
        ctx.addValidationError("deviceId is required");
        when(pipeline.process(anyString())).thenReturn(ctx);

        mvc.perform(post("/api/telemetry/ingest")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("bad"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors[0]").value("deviceId is required"));
    }

    @Test
    void ingestBatch_returnsAllResults() throws Exception {
        TelemetryPipelineContext ctx = successCtx();
        when(pipeline.process(anyString())).thenReturn(ctx);

        mvc.perform(post("/api/telemetry/ingest/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"OUTAGE|DEV-001|2024-01-15T10:30:00Z|1500.0|WEST\",\"PRICE|NODE-1|2024-01-15T10:30:00Z|45.0|EAST\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getEvents_returnsEmptyList() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mvc.perform(get("/api/telemetry/events"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getEventsByType_returnsFilteredList() throws Exception {
        when(repository.findByType(TelemetryEventType.OUTAGE)).thenReturn(List.of());

        mvc.perform(get("/api/telemetry/events/OUTAGE"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private TelemetryPipelineContext successCtx() {
        TelemetryPipelineContext ctx = new TelemetryPipelineContext("raw");
        ctx.setEventType(TelemetryEventType.OUTAGE);
        ctx.setDeviceId("DEV-001");
        ctx.setPersistedId(42L);
        return ctx;
    }
}
