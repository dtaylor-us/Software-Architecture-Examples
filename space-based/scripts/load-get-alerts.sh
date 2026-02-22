#!/usr/bin/env bash
# Load test the read path: GET /active-alerts.
# Usage: ./load-get-alerts.sh [requests] [concurrency]
# With hey: ./load-get-alerts.sh 2000 30
# With curl only: ./load-get-alerts.sh 100 5
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REQUESTS="${1:-200}"
CONC="${2:-10}"
BASE_URL="${BASE_URL:-http://localhost:8080}"

if command -v hey &> /dev/null; then
  echo "→ hey: $REQUESTS requests, $CONC concurrent, GET ${BASE_URL}/active-alerts"
  hey -n "$REQUESTS" -c "$CONC" "${BASE_URL}/active-alerts"
else
  echo "→ curl loop: $REQUESTS requests, $CONC concurrent"
  start=$(date +%s)
  sent=0
  for i in $(seq 1 "$REQUESTS"); do
    curl -s -o /dev/null "${BASE_URL}/active-alerts" &
    sent=$(( sent + 1 ))
    if [ $(( sent % CONC )) -eq 0 ]; then
      wait
    fi
  done
  wait
  end=$(date +%s)
  elapsed=$(( end - start ))
  rps=$(( REQUESTS / (elapsed > 0 ? elapsed : 1) ))
  echo "✓ Sent $REQUESTS requests in ${elapsed}s (~$rps req/s)"
fi
