# Load test scripts

All scripts use **curl** (no extra install) unless noted. Override the API base URL with:

```bash
export BASE_URL=http://localhost:8080
```

| Script | What it does |
|--------|----------------|
| **health.sh** | GET /health — check API is up |
| **post-bulk.sh** | POST 4 price updates once — quick smoke |
| **get-active-alerts.sh** | GET /active-alerts once |
| **generate-payload.sh** | Create a JSON file of many updates (for load) |
| **load-curl.sh** | Repeated POSTs using only curl |
| **load-hey.sh** | Repeated POSTs using **hey** (faster, needs Go) |
| **load-get-alerts.sh** | Repeated GET /active-alerts (hey or curl) |
| **run-smoke.sh** | Health + one POST + one GET (no tools needed) |
| **run-load.sh** | Generate payload, then POST load, then GET load |
| **load-multi.sh** | Round-robin POST load across multiple URLs (for horizontal scaling). Set `MULTI_BASE_URLS=http://localhost:8080,http://localhost:8081` |
| **load-k6.js** | k6 script: mixed POST + GET load (run with `k6 run scripts/load-k6.js`) |

**Quick smoke (from project root):**

```bash
./scripts/run-smoke.sh
```

**Full load (generate 500-update payload, then 200 POSTs, then GETs):**

```bash
./scripts/run-load.sh
# Or: ./scripts/run-load.sh 500 500 30   # 500 updates in payload, 500 POST requests, 30 concurrent
```

**With k6:**

```bash
k6 run scripts/load-k6.js
```

See [docs/LOAD_TESTING.md](../docs/LOAD_TESTING.md) for full documentation.
