#!/usr/bin/env bash
# Full load test: generate payload, then run POST load (hey or curl), then GET /active-alerts load.
# Usage: ./run-load.sh [num_updates_in_payload] [post_requests] [concurrency]
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PAYLOAD="$SCRIPT_DIR/payload.json"
UPDATES="${1:-500}"
REQUESTS="${2:-200}"
CONC="${3:-20}"
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "=== Load test (BASE_URL=$BASE_URL) ==="
echo
"$SCRIPT_DIR/health.sh"
echo
"$SCRIPT_DIR/generate-payload.sh" "$UPDATES" 50 "$PAYLOAD"
echo
echo "--- POST /price-updates load ---"
if command -v hey &> /dev/null; then
  "$SCRIPT_DIR/load-hey.sh" "$PAYLOAD" "$REQUESTS" "$CONC"
else
  "$SCRIPT_DIR/load-curl.sh" "$PAYLOAD" "$REQUESTS" "$CONC"
fi
echo
echo "--- GET /active-alerts load ---"
"$SCRIPT_DIR/load-get-alerts.sh" "$(( REQUESTS / 2 ))" "$CONC"
echo
echo "=== Load test done ==="
