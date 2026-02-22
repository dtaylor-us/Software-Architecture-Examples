# Load Testing: Space-Based Energy API

This guide walks you through load testing the API with **ready-made scripts** and optional tools. The goal is to simulate many incoming price updates and read traffic so you can observe throughput, latency, and correctness (alerts, TTL, persistence).

---

## Prerequisites

1. **Redis** and the **application** must be running:
   ```bash
   docker compose up -d
   ./mvnw spring-boot:run
   ```
2. **curl** — used by all scripts (available on macOS/Linux).
3. **Optional:** **hey** (for faster load tests): `go install github.com/rakyll/hey@latest`
4. **Optional:** **k6** (for scripted scenarios): [k6.io](https://k6.io/docs/get-started/installation/)

Scripts live in **`scripts/`**. Run them from the **project root** or from `scripts/`; they use `BASE_URL=http://localhost:8080` by default.

---

## Quick start (copy-paste)

**1. Smoke test (health + one POST + one GET)** — no extra tools:

```bash
./scripts/run-smoke.sh
```

You should see:
- `✓ Health OK (HTTP 200)` and a JSON body with `"status":"UP"`
- A JSON response from `POST /price-updates` with `accepted: 4` and optionally `alertsRaised`
- A JSON array from `GET /active-alerts` (possibly `[]`)

**2. Generate a large payload and run a short load test** (curl-only):

```bash
./scripts/generate-payload.sh 500 50
./scripts/load-curl.sh scripts/payload.json 100 10
```

**3. Same thing, but with one command** (uses hey if installed, else curl):

```bash
./scripts/run-load.sh 500 200 20
```

---

## Script reference

### Health and single requests

| Command | Description |
|---------|-------------|
| `./scripts/health.sh` | GET /health. Exits with 1 if the API is down. |
| `./scripts/post-bulk.sh` | POST 4 price updates in one request. Good smoke test. |
| `./scripts/get-active-alerts.sh` | GET /active-alerts once. |

**Example output (health):**
```text
→ GET http://localhost:8080/health
{"arch":"space-based","status":"UP"}
✓ Health OK (HTTP 200)
```

---

### Payload generation

| Command | Description |
|---------|-------------|
| `./scripts/generate-payload.sh` | Default: 500 updates, 50 nodes → `scripts/payload.json` |
| `./scripts/generate-payload.sh 1000 100` | 1000 updates, 100 nodes → `scripts/payload.json` |
| `./scripts/generate-payload.sh 500 50 /tmp/my.json` | Custom output file |

About 10% of generated updates are “spikes” (higher price) so the spike-detection logic can fire. The script uses only bash (no jq required).

---

### Load tests (POST /price-updates)

| Command | Description |
|---------|-------------|
| `./scripts/load-curl.sh [file] [requests] [concurrency]` | Repeated POSTs using curl. Defaults: `scripts/payload.json`, 100, 5. |
| `./scripts/load-hey.sh [file] [requests] [concurrency]` | Same idea with **hey** (better latency stats). Defaults: 1000, 50. If hey is not installed, falls back to `load-curl.sh`. |

**Examples:**
```bash
# Generate payload then run 200 POSTs, 20 concurrent (curl)
./scripts/generate-payload.sh 500 50
./scripts/load-curl.sh scripts/payload.json 200 20

# With hey: 1000 requests, 50 concurrent
./scripts/load-hey.sh scripts/payload.json 1000 50
```

---

### Load test (GET /active-alerts)

| Command | Description |
|---------|-------------|
| `./scripts/load-get-alerts.sh [requests] [concurrency]` | Repeated GET /active-alerts. Uses hey if available, else curl loop. Defaults: 200, 10. |

**Example:**
```bash
./scripts/load-get-alerts.sh 2000 30
```

---

### One-shot flows

| Command | Description |
|---------|-------------|
| `./scripts/run-smoke.sh` | Health → one bulk POST → one GET. No dependencies beyond curl. |
| `./scripts/run-load.sh [updates] [post_requests] [concurrency]` | Health → generate payload → POST load → GET load. Defaults: 500, 200, 20. |

---

### k6 (optional)

k6 runs a script that mixes POST and GET traffic with configurable VUs and duration.

```bash
k6 run scripts/load-k6.js
```

Override base URL:
```bash
BASE_URL=http://localhost:8080 k6 run scripts/load-k6.js
```

The script runs 20 VUs posting price updates and 10 VUs calling GET /active-alerts for 30 seconds and reports pass/fail and latency.

---

## What to look for

- **Health:** Scripts exit 0 only when /health returns 200. If health fails, fix the app/Redis first.
- **POST response:** JSON with `accepted` (number of updates) and `alertsRaised`. After sending several bulks with spikes, `alertsRaised` may be &gt; 0.
- **GET /active-alerts:** Returns an array of active alerts; after TTL (default 5 min) they disappear. Empty `[]` is valid.
- **Throughput:** With **hey**, check “Requests/sec” and latency distribution (e.g. p50, p95). With **curl** scripts, the script prints approximate req/s.
- **Persistence:** Alert history is written asynchronously to the DB. Use H2 console (`/h2-console`) or query Postgres to confirm rows in `alert_history`.

---

## Using a different host or port

Set `BASE_URL` before running any script:

```bash
export BASE_URL=http://myhost:8080
./scripts/run-smoke.sh
```

Or inline:

```bash
BASE_URL=http://myhost:8080 ./scripts/load-hey.sh scripts/payload.json 500 25
```

---

## Troubleshooting

| Problem | What to do |
|--------|------------|
| `Connection refused` | Start the app (`./mvnw spring-boot:run`) and ensure Redis is up (`docker compose up -d`). |
| `health.sh` exits 1 | Check that the app is listening on the port in `BASE_URL` and that `/health` returns 200. |
| `payload.json not found` | Run `./scripts/generate-payload.sh` first (or pass the path to the script). |
| `hey not found` | Install hey or rely on `load-curl.sh` / `run-load.sh` (they fall back to curl). |
| Very low req/s | Try lower concurrency first; check CPU/Redis; run multiple API instances behind a load balancer to scale out. |

---

## Horizontal scaling: multi-instance load testing

The API is stateless and shares Redis, so you can run **multiple instances** and put a **load balancer** in front to increase total throughput. Follow these steps to measure the effect.

---

### Step 1: Start two API instances

Use two terminals (same machine is fine). Each instance uses the **same Redis** and a **different port**.

**Terminal 1 — instance on 8080:**
```bash
cd /path/to/space-based
docker compose up -d
PORT=8080 ./mvnw spring-boot:run
```

**Terminal 2 — instance on 8081:**
```bash
cd /path/to/space-based
PORT=8081 ./mvnw spring-boot:run
```

Leave both running. Confirm both are up:
```bash
curl -s http://localhost:8080/health
curl -s http://localhost:8081/health
```

---

### Step 2: Baseline — load test a single instance

Measure throughput with **one** instance so you have a number to compare.

```bash
./scripts/generate-payload.sh 500 50
BASE_URL=http://localhost:8080 ./scripts/load-hey.sh scripts/payload.json 500 30
```

Note the **Requests/sec** (and optionally p95 latency). Example: `Requests/sec: 85.3`. That’s your **baseline** for one instance.

---

### Step 3a: Load test both instances (round-robin script)

Send the same total load but **spread across both** URLs. The script round-robins each request to 8080 or 8081 (like a simple load balancer would).

```bash
MULTI_BASE_URLS="http://localhost:8080,http://localhost:8081" ./scripts/load-multi.sh scripts/payload.json 500 30
```

Compare the **req/s** to the baseline. With two instances and no other bottleneck (CPU, Redis), you should see roughly **up to 2×** the single-instance throughput.

To use **hey** against each URL in parallel (two separate streams) and add their throughput:

```bash
# Split load: 250 requests each, 15 concurrent each
BASE_URL=http://localhost:8080 hey -n 250 -c 15 -m POST -D scripts/payload.json -T application/json http://localhost:8080/price-updates &
BASE_URL=http://localhost:8081 hey -n 250 -c 15 -m POST -D scripts/payload.json -T application/json http://localhost:8081/price-updates &
wait
```

Add the two “Requests/sec” lines to get combined throughput.

---

### Step 3b: Optional — put a real load balancer in front

Use **nginx** as a reverse proxy so all traffic goes to one URL and nginx distributes to both instances.

**Start the load balancer:**
```bash
docker compose -f docker-compose.loadbalancer.yml up -d
```

This starts nginx on **port 8088**, forwarding to `host.docker.internal:8080` and `host.docker.internal:8081` in round-robin.

**Run load against the LB (single entry point):**
```bash
BASE_URL=http://localhost:8088 ./scripts/load-hey.sh scripts/payload.json 500 30
```

Or with the round-robin script (same effect as hitting the LB):
```bash
BASE_URL=http://localhost:8088 ./scripts/run-load.sh 500 500 30
```

You should again see higher total throughput than the single-instance baseline, and nginx will spread requests across both app instances.

---

### Step 4: Compare and interpret

| Setup | What you ran | What to compare |
|-------|----------------|-----------------|
| **1 instance** | `BASE_URL=http://localhost:8080` load script | Baseline req/s |
| **2 instances (round-robin)** | `MULTI_BASE_URLS="...8080,...8081" ./scripts/load-multi.sh ...` | Total req/s ≈ up to 2× baseline |
| **2 instances (nginx LB)** | `BASE_URL=http://localhost:8088` load script | Total req/s ≈ up to 2× baseline |

If throughput doesn’t scale linearly, the bottleneck may be **Redis**, **CPU**, or **network**. The point is: adding more **stateless** API instances and distributing traffic increases capacity because each instance shares only Redis (the “space”), not in-process state.

---

### Script reference (multi-instance)

| Command | Description |
|---------|-------------|
| `MULTI_BASE_URLS="http://localhost:8080,http://localhost:8081" ./scripts/load-multi.sh [file] [requests] [concurrency]` | Round-robin POST load across the listed URLs. Defaults: payload.json, 200, 20. |

Load balancer (optional):

```bash
docker compose -f docker-compose.loadbalancer.yml up -d   # nginx on 8088
BASE_URL=http://localhost:8088 ./scripts/load-hey.sh scripts/payload.json 500 30
```

**Note (Linux):** The nginx config uses `host.docker.internal` to reach the app on the host. Docker 20.10+ adds this automatically with `host-gateway`. If it’s missing, add `extra_hosts: ["host.docker.internal:host-gateway"]` to the `lb` service or set the host’s IP explicitly in `observability/nginx/nginx-lb.conf`.
