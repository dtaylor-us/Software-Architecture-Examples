package com.gridops.microkernel.host;

import com.gridops.microkernel.core.engine.AlertRuleEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EvaluateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPlugins_returnsList() throws Exception {
        mockMvc.perform(get("/api/plugins"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.plugins").isArray())
            .andExpect(jsonPath("$.plugins.length()").value(3));
    }

    @Test
    void evaluate_priceSpike_returnsAlertsAndPluginsFired() throws Exception {
        String body = """
            {
              "eventId": "evt-1",
              "eventType": "price",
              "payload": { "price": 200 }
            }
            """;
        mockMvc.perform(post("/api/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.eventId").value("evt-1"))
            .andExpect(jsonPath("$.alerts").isArray())
            .andExpect(jsonPath("$.pluginsFired").isArray())
            .andExpect(jsonPath("$.pluginsFired[0]").value("price-spike"));
    }

    @Test
    void evaluate_forecastRamp_returnsAlerts() throws Exception {
        String body = """
            {
              "eventType": "forecast-ramp",
              "payload": { "rampMw": 600 }
            }
            """;
        mockMvc.perform(post("/api/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pluginsFired").isArray())
            .andExpect(jsonPath("$.alerts[?(@.pluginId=='forecast-ramp')]").isArray());
    }

    @Test
    void evaluate_outageRisk_returnsAlerts() throws Exception {
        String body = """
            {
              "eventType": "outage-risk",
              "payload": { "reserveMarginPct": 10 }
            }
            """;
        mockMvc.perform(post("/api/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pluginsFired").isArray())
            .andExpect(jsonPath("$.alerts[?(@.pluginId=='outage-capacity-risk')]").isArray());
    }

    // --- Add/remove plugin at runtime (validation) ---

    @Test
    void addPlugin_missingPluginId_returns400() throws Exception {
        mockMvc.perform(post("/api/plugins")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("pluginId is required"));
    }

    @Test
    void addPlugin_invalidPluginId_returns400() throws Exception {
        mockMvc.perform(post("/api/plugins")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pluginId\": \"no-such-plugin\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void addPlugin_alreadyRegistered_returns409() throws Exception {
        mockMvc.perform(post("/api/plugins")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pluginId\": \"price-spike\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Plugin already registered: price-spike"));
    }

    @Test
    void removePlugin_notRegistered_returns404() throws Exception {
        mockMvc.perform(delete("/api/plugins/nonexistent-plugin"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Plugin not registered: nonexistent-plugin"));
    }

    @Test
    void removePlugin_thenAdd_roundTrip() throws Exception {
        // Remove one plugin
        mockMvc.perform(delete("/api/plugins/price-spike"))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/plugins"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.plugins.length()").value(2));

        // Add it back (valid plugin id, was removed)
        mockMvc.perform(post("/api/plugins")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pluginId\": \"price-spike\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("price-spike"))
            .andExpect(jsonPath("$.name").value("Price Spike Rule"));
        mockMvc.perform(get("/api/plugins"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.plugins.length()").value(3));
    }
}
