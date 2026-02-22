package com.gridops.forecast;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ForecastServiceImpl implements ForecastService {

    private final ForecastRepository repository;

    public ForecastServiceImpl(ForecastRepository repository) {
        this.repository = repository;
    }

    @Override
    public ForecastRecord createForecast(CreateForecastCommand command) {
        String id = "FC-" + UUID.randomUUID().toString().substring(0, 8);
        ForecastRecord record = new ForecastRecord(
                id,
                command.zoneId(),
                command.at(),
                command.mwValue(),
                command.horizon()
        );
        return repository.save(record);
    }

    @Override
    public List<ForecastRecord> getForecastsForWindow(String zoneId, Instant from, Instant to) {
        return repository.findByZoneIdAndTimeWindow(zoneId, from, to);
    }

    @Override
    public List<ForecastRecord> listByZone(String zoneId) {
        return repository.findByZoneId(zoneId);
    }
}
