package com.example.spacebased.api;

import com.example.spacebased.dto.ActiveAlert;
import com.example.spacebased.dto.PriceUpdate;
import com.example.spacebased.space.EnergySpace;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PriceUpdateController.class)
class PriceUpdateControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EnergySpace energySpace;

    @Test
    void health_returnsUp() throws Exception {
        mvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.arch").value("space-based"));
    }

    @Test
    void postPriceUpdates_acceptsBulkAndReturnsAlertsRaised() throws Exception {
        String body = """
            [
              {"nodeId":"NODE-001","priceMwh":100},
              {"nodeId":"NODE-001","priceMwh":250}
            ]
            """;
        ActiveAlert alert = ActiveAlert.builder()
            .alertId("NODE-001:SPIKE:1")
            .nodeId("NODE-001")
            .type("SPIKE")
            .priceMwh(new BigDecimal("250"))
            .thresholdOrAverage(new BigDecimal("100"))
            .raisedAt(Instant.now())
            .ttlSeconds(300)
            .build();
        when(energySpace.writePriceUpdates(anyList())).thenReturn(List.of(alert));

        mvc.perform(post("/price-updates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accepted").value(2))
            .andExpect(jsonPath("$.alertsRaised").value(1))
            .andExpect(jsonPath("$.alerts[0].nodeId").value("NODE-001"));
    }

    @Test
    void getActiveAlerts_returnsListFromSpace() throws Exception {
        when(energySpace.getActiveAlerts()).thenReturn(List.of());

        mvc.perform(get("/active-alerts"))
            .andExpect(status().isOk())
            .andExpect(content().string("[]"));
    }
}
