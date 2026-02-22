package com.gridops.api;

import com.gridops.forecast.ForecastService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/forecasts")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @PostMapping
    public ResponseEntity<ForecastService.ForecastRecord> createForecast(@Valid @RequestBody CreateForecastRequest req) {
        ForecastService.CreateForecastCommand cmd = new ForecastService.CreateForecastCommand(
                req.zoneId(),
                req.at() != null ? req.at() : Instant.now(),
                req.mwValue(),
                req.horizon() != null ? req.horizon() : "DAY-AHEAD"
        );
        return ResponseEntity.ok(forecastService.createForecast(cmd));
    }

    @GetMapping
    public List<ForecastService.ForecastRecord> listByZone(@RequestParam @NotBlank String zoneId) {
        return forecastService.listByZone(zoneId);
    }

    @GetMapping("/window")
    public List<ForecastService.ForecastRecord> getWindow(
            @RequestParam @NotBlank String zoneId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return forecastService.getForecastsForWindow(zoneId, from, to);
    }

    public record CreateForecastRequest(String zoneId, Instant at, double mwValue, String horizon) {}
}
