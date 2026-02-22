#!/usr/bin/env bash
# Load test using hey (install: go install github.com/rakyll/hey@latest).
# Usage: ./load-hey.sh [payload_file] [total_requests] [concurrency]
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PAYLOAD="${1:-$SCRIPT_DIR/payload.json}"
REQUESTS="${2:-1000}"
CONC="${3:-50}"
BASE_URL="${BASE_URL:-http://localhost:8080}"

if ! command -v hey &> /dev/null; then
  echo "hey not found. Install with: go install github.com/rakyll/hey@latest"
  echo "Falling back to load-curl.sh..."
  exec "$SCRIPT_DIR/load-curl.sh" "$PAYLOAD" "$REQUESTS" "$CONC"
fi

if [ ! -f "$PAYLOAD" ]; then
  echo "Payload file not found: $PAYLOAD"
  echo "Run: $SCRIPT_DIR/generate-payload.sh 500 50 $PAYLOAD"
  exit 1
fi

echo "→ hey: $REQUESTS requests, $CONC concurrent, POST $PAYLOAD"
hey -n "$REQUESTS" -c "$CONC" -m POST -D "$PAYLOAD" -T "application/json" "${BASE_URL}/price-updates"
