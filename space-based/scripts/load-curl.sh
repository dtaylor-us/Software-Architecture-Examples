#!/usr/bin/env bash
# Load test using only curl: repeated POSTs with a payload file.
# Usage: ./load-curl.sh [payload_file] [num_requests] [concurrency]
# Defaults: scripts/payload.json (create with generate-payload.sh), 100, 5
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PAYLOAD="${1:-$SCRIPT_DIR/payload.json}"
REQUESTS="${2:-100}"
CONC="${3:-5}"
BASE_URL="${BASE_URL:-http://localhost:8080}"

if [ ! -f "$PAYLOAD" ]; then
  echo "Payload file not found: $PAYLOAD"
  echo "Run: $SCRIPT_DIR/generate-payload.sh 500 50 $PAYLOAD"
  exit 1
fi

echo "→ Load test: $REQUESTS requests, $CONC concurrent, POST $PAYLOAD → $BASE_URL/price-updates"
start=$(date +%s)
sent=0
for i in $(seq 1 "$REQUESTS"); do
  curl -s -o /dev/null -X POST "${BASE_URL}/price-updates" \
    -H "Content-Type: application/json" -d @"$PAYLOAD" &
  sent=$(( sent + 1 ))
  if [ $(( sent % CONC )) -eq 0 ]; then
    wait
  fi
done
wait
end=$(date +%s)
elapsed=$(( end - start ))
[ "$elapsed" -lt 1 ] && elapsed=1
rps=$(( REQUESTS / elapsed ))
echo "✓ Sent $REQUESTS requests in ${elapsed}s (~$rps req/s)"
