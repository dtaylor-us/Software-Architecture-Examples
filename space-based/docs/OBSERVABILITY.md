# Observability: Grafana, Prometheus, OpenTelemetry, Jaeger

This project includes an observability stack so you can view **metrics** (Prometheus + Grafana) and **traces** (OpenTelemetry → OTEL Collector → Jaeger, viewed in Grafana or Jaeger UI).

---

## What runs where

| Component | Role | URL (when running) |
|-----------|------|--------------------|
| **Prometheus** | Scrapes metrics from the app (`/actuator/prometheus`) | http://localhost:9090 |
| **Grafana** | Dashboards and queries (Prometheus + Jaeger datasources) | http://localhost:3000 (login: admin / admin) |
| **OTEL Collector** | Receives OTLP traces from the app, forwards to Jaeger | OTLP gRPC: localhost:4317, HTTP: localhost:4318 |
| **Jaeger** | Trace storage and UI | http://localhost:16686 |

The **app** exposes:
- `/actuator/prometheus` — for Prometheus scrape
- `/actuator/health`, `/actuator/metrics` — for health and raw metrics
- Sends **traces** via OTLP to the OTEL Collector (or directly to Jaeger if you skip the collector).

---

## Quick start

**1. Start Redis (and optionally Postgres):**
```bash
docker compose up -d
```

**2. Start the observability stack:**
```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

**3. Start the application** (on the host so Prometheus can scrape it):
```bash
# App uses OTLP HTTP; collector HTTP port is 4318 (gRPC is 4317). Default is already 4318.
./mvnw spring-boot:run
# Or explicitly: OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318 ./mvnw spring-boot:run
```

**4. Open:**
- **Grafana:** http://localhost:3000 (admin / admin). The **Prometheus** and **Jaeger** datasources are pre-provisioned. A **Space-Based Energy API** dashboard is available (HTTP rate, latency, JVM).
- **Prometheus:** http://localhost:9090 → Status → Targets. The `space-based-energy` target should be **UP** (scrapes `host.docker.internal:8080`).
- **Jaeger:** http://localhost:16686 — select service `space-based-energy` and **Find Traces** after sending some traffic.

**5. Generate traffic** so metrics and traces appear:
```bash
./scripts/run-smoke.sh
./scripts/run-load.sh 500 100 20
```

---

## Configuration

### App (Spring Boot)

- **Metrics:** Actuator exposes `prometheus`, `metrics`, `health`, `info`. Scrape endpoint: `/actuator/prometheus`.
- **Tracing:** The app sends traces via **OTLP HTTP** (not gRPC). Use the collector’s **HTTP** port **4318** (gRPC is 4317). Set `management.otlp.tracing.endpoint` or **`OTEL_EXPORTER_OTLP_ENDPOINT`** to `http://localhost:4318`. Default in `application.yml` is already 4318.

When the app runs on the host and the collector runs in Docker, the default works. To disable trace export when not using the observability stack, set `OTEL_EXPORTER_OTLP_ENDPOINT=` (empty) or point to a non-existent host.

### Prometheus

- Config: **`observability/prometheus/prometheus.yml`**
- Scrapes the app at `host.docker.internal:8080` (app on host). Scrape interval: 10s for the app, 15s default.

If the app runs in Docker (e.g. as a service), change the Prometheus target to the app service name and port (e.g. `http://app:8080`).

### OTEL Collector

- Config: **`observability/otel-collector/otel-collector-config.yaml`**
- Receives OTLP (gRPC 4317, HTTP 4318), batches, and exports traces to Jaeger. Debug exporter is enabled for troubleshooting.

### Grafana

- **Datasources:** Provisioned from **`observability/grafana/provisioning/datasources/datasources.yaml`** (Prometheus as default, Jaeger for traces).
- **Dashboards:** Provisioned from **`observability/grafana/provisioning/dashboards/`**. The **Space-Based Energy API** dashboard shows HTTP request rate, p95 latency, JVM heap, and threads.

---

## Ports summary

| Port | Service |
|------|---------|
| 3000 | Grafana |
| 9090 | Prometheus |
| 4317 | OTEL Collector (OTLP gRPC) — app sends traces here |
| 4318 | OTEL Collector (OTLP HTTP) |
| 16686 | Jaeger UI |

---

## Troubleshooting

| Issue | Check |
|-------|--------|
| Prometheus target DOWN | App must be running on the host on port 8080. On Linux, ensure `host.docker.internal` resolves (Docker 20.10+). If the app is in Docker, point Prometheus at the app service. |
| No traces in Jaeger | Set `OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317` and restart the app. Send some HTTP requests. In Jaeger, select service `space-based-energy`. |
| Grafana “No data” | Ensure Prometheus has scraped the app (see Prometheus → Targets). Expand the time range. |
| Dashboard panels empty | Metric names depend on Spring Boot/Micrometer; if your app uses different labels, adjust the panel queries in Grafana. |

---

## Optional: App in Docker with observability

To run the app in Docker and have Prometheus scrape it, add an `app` service to `docker-compose.observability.yml` (build from the project root, set `REDIS_HOST=redis`, `OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317`). Then in **`observability/prometheus/prometheus.yml`** change the scrape target to `app:8080` instead of `host.docker.internal:8080`.
