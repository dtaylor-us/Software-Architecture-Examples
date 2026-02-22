package com.gridops.forecast;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "forecasts")
public class ForecastEntity {

    @Id
    private String id;
    @Column(nullable = false, length = 64)
    private String zoneId;
    @Column(nullable = false)
    private Instant at;
    @Column(nullable = false)
    private double mwValue;
    @Column(nullable = false, length = 64)
    private String horizon;

    protected ForecastEntity() {}

    public ForecastEntity(String id, String zoneId, Instant at, double mwValue, String horizon) {
        this.id = id;
        this.zoneId = zoneId;
        this.at = at;
        this.mwValue = mwValue;
        this.horizon = horizon;
    }

    public static ForecastEntity from(ForecastService.ForecastRecord r) {
        return new ForecastEntity(r.id(), r.zoneId(), r.at(), r.mwValue(), r.horizon());
    }

    public ForecastService.ForecastRecord toRecord() {
        return new ForecastService.ForecastRecord(id, zoneId, at, mwValue, horizon);
    }

    public String getId() { return id; }
    public String getZoneId() { return zoneId; }
    public Instant getAt() { return at; }
    public double getMwValue() { return mwValue; }
    public String getHorizon() { return horizon; }
}
