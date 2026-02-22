# Space-Based Architecture: Energy Operations Example

## Overview

You send in **electricity prices** for grid nodes (e.g. many at once during a price spike). The app stores the latest price and a short **rolling window** per node in **Redis**. If a price jumps above a threshold (e.g. 2× the recent average), it creates a **spike alert** that stays active for a few minutes (TTL); when things calm down, the alert expires on its own. You can ask for **active alerts** anytime. A **background worker** copies alert history into a database (H2 or Postgres) so you have a durable record. The API is stateless so you can run more instances behind a load balancer and they all share the same Redis.

**Rolling window:** For each node we keep the last N minutes of prices (e.g. 15). That list is the “rolling window.” We use it to compute a **recent average**. When a new price arrives, we compare it to that average: if the new price is much higher (e.g. more than 2× the average), we treat it as a spike and raise an alert. So the window’s job is to answer “what’s normal for this node lately?” so we can tell when something is abnormally high.

## Quick Start

```bash
# Start Redis (and optional Postgres)
docker compose up -d

# Run the app (uses H2 in-memory DB by default)
# If you don't have ./mvnw, generate it: mvn -N wrapper:wrapper
./mvnw spring-boot:run
# Or: mvn spring-boot:run

# With Postgres
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | `/price-updates` | Bulk price updates (JSON array). Updates the space and evaluates spike alerts (TTL). |
| GET | `/active-alerts` | List active alerts from the space (expired TTL = gone). |
| GET | `/health` | Health check. |

**API docs (Swagger UI):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — OpenAPI spec at `/v3/api-docs`.

## Tech Stack

- **Java 21**, **Spring Boot 3.x**, **Maven**
- **Redis** — the "space" (hot state: latest price, rolling window, active alerts with TTL)
- **H2** (default) or **Postgres** — alert history via async persistence worker

## Load testing

From the project root (API and Redis running):

```bash
./scripts/run-smoke.sh          # Health + one POST + one GET (no extra tools)
./scripts/run-load.sh           # Generate payload, then POST + GET load (uses hey if installed, else curl)
```

See **[docs/LOAD_TESTING.md](docs/LOAD_TESTING.md)** for full steps and **[scripts/README.md](scripts/README.md)** for all scripts.

## Observability (Grafana, Prometheus, OTEL, Jaeger)

Start the observability stack and the app with tracing:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318 ./mvnw spring-boot:run
```

- **Grafana:** http://localhost:3000 (admin/admin) — preconfigured Prometheus + Jaeger and a Space-Based Energy dashboard.
- **Prometheus:** http://localhost:9090
- **Jaeger (traces):** http://localhost:16686

See **[docs/OBSERVABILITY.md](docs/OBSERVABILITY.md)** for details and troubleshooting.

## Docs

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) — What the space is, partitioning, scaling, strengths/weaknesses, Mermaid diagram
- [LOAD_TESTING.md](docs/LOAD_TESTING.md) — Load testing with scripts (curl, hey, k6), what to observe, troubleshooting
- [OBSERVABILITY.md](docs/OBSERVABILITY.md) — Grafana, Prometheus, OpenTelemetry, Jaeger setup and usage

## Configuration

- `app.space.price-window-minutes` — rolling window for spike detection (default 15)
- `app.space.alert-ttl-seconds` — TTL for active alerts (default 300)
- `app.space.spike-threshold-multiplier` — alert when price > this × rolling average (default 2.0)
- `REDIS_HOST`, `REDIS_PORT` — Redis connection (default localhost:6379)
- For Postgres: `SPRING_PROFILES_ACTIVE=postgres` and set `POSTGRES_*` or use defaults in `application-postgres.yml`
