# GridOps Telemetry Pipeline — Pipeline (Pipes & Filters) Architecture

An educational example of the **Pipeline (Pipes & Filters)** architectural pattern using the GridOps energy-grid domain.  Raw telemetry events (outages, forecasts, electricity prices) are produced by grid devices and market nodes, and must be cleaned, validated, enriched, and stored before they can be used for operations and reporting.

---

## Architecture overview

```
Raw Input (String line or JSON blob)
        │
  ┌─────▼──────┐
  │  1. Ingest │  — null/blank guard, whitespace trim
  └─────┬──────┘
        │
  ┌─────▼──────┐
  │  2. Parse  │  — detect format (JSON / pipe-delimited), extract fields
  └─────┬──────┘
        │
  ┌────────────┐
  │ 3. Normalize│  — canonicalize region, round values, assign default units
  └─────┬──────┘
        │
  ┌────────────┐
  │ 4. Validate│  — check required fields, ranges, timestamp sanity
  └─────┬──────┘
        │
  ┌────────────┐
  │  5. Enrich │  — add grid-zone, severity/category labels, pipeline metadata
  └─────┬──────┘
        │
  ┌────────────┐
  │  6. Filter │  — drop UNKNOWN types and zero-value heartbeats
  └─────┬──────┘
        │
  ┌────────────┐
  │  7. Persist│  — save to H2/RDBMS via Spring Data JPA
  └─────┬──────┘
        │
  ┌──────────────────┐
  │ 8. PublishSummary│  — emit Spring ApplicationEvent (or Kafka in prod)
  └──────────────────┘
```

The pipeline is assembled by **Spring's ordered `List<PipelineStage>` injection** — stages declare their position with `@Order(n)`.  If any stage marks the context **invalid** or **filtered**, the runner skips all remaining main stages and jumps directly to `PublishSummary` so every ingest attempt produces an observable outcome.

---

## Supported input formats

### JSON blob
```json
{"type":"OUTAGE",   "deviceId":"DEV-001", "timestamp":"2024-01-15T10:30:00Z", "value":1500.0, "region":"WEST"}
{"type":"FORECAST", "deviceId":"METER-42","timestamp":"2024-01-15T10:30:00Z", "value":250.5,  "region":"EAST", "unit":"MW"}
{"type":"PRICE",    "deviceId":"NODE-99", "timestamp":"2024-01-15T10:30:00Z", "value":45.75,  "region":"NORTH","unit":"$/MWh"}
```

### Pipe-delimited line
```
TYPE|DEVICE_ID|TIMESTAMP|VALUE[|REGION]

OUTAGE|DEV-001|2024-01-15T10:30:00Z|1500.0|WEST
FORECAST|METER-42|2024-01-15T10:30:00Z|250.5|EAST
PRICE|NODE-99|2024-01-15T10:30:00Z|45.75|N
```

Region abbreviations (`W`, `E`, `N`, `S`, `NW`, `SE`) are expanded during Normalization.

---

## Quick start

```bash
cd pipeline
./mvnw spring-boot:run
```

The application starts on port **8080** with an in-memory H2 database.

### H2 console
<http://localhost:8080/h2-console>  
JDBC URL: `jdbc:h2:mem:telemetrydb`

---

## REST API

### Ingest a single event
```bash
# JSON blob
curl -X POST http://localhost:8080/api/telemetry/ingest \
  -H "Content-Type: text/plain" \
  -d '{"type":"OUTAGE","deviceId":"DEV-001","timestamp":"2024-01-15T10:30:00Z","value":1500.0,"region":"WEST"}'

# Pipe-delimited
curl -X POST http://localhost:8080/api/telemetry/ingest \
  -H "Content-Type: text/plain" \
  -d 'FORECAST|METER-42|2024-01-15T10:30:00Z|250.5|EAST'
```

### Ingest a batch
```bash
curl -X POST http://localhost:8080/api/telemetry/ingest/batch \
  -H "Content-Type: application/json" \
  -d '["OUTAGE|DEV-001|2024-01-15T10:30:00Z|1500.0|WEST","PRICE|NODE-99|2024-01-15T10:30:00Z|45.75|NORTH"]'
```

### Query persisted events
```bash
# All events
curl http://localhost:8080/api/telemetry/events

# By type
curl http://localhost:8080/api/telemetry/events/OUTAGE
curl http://localhost:8080/api/telemetry/events/FORECAST
curl http://localhost:8080/api/telemetry/events/PRICE
```

---

## Build & test

```bash
# Compile
./mvnw clean compile

# Run all tests
./mvnw test

# Package
./mvnw clean package
```

---

## Tech stack

| Component | Version |
|-----------|---------|
| Java | 21 |
| Spring Boot | 3.2.5 |
| Spring Data JPA | (managed by Boot) |
| H2 | (managed by Boot) |
| Lombok | (managed by Boot) |
| JUnit 5 / AssertJ | (managed by Boot) |

---

## Key design decisions

| Decision | Rationale |
|----------|-----------|
| Single mutable context object | Avoids cascading generic types; stages remain decoupled |
| `@Order` for stage sequencing | Sequence is self-documenting on each stage class |
| PublishSummary always runs | Guarantees observability even for failed/filtered events |
| Spring `ApplicationEvent` for summaries | Decouples pipeline from consumers; swap to Kafka with no stage changes |
| H2 in-memory database | Zero-infrastructure setup for learning; swap datasource for production |
