#!/usr/bin/env bash
# Run POST load across multiple API instances (round-robin). Simulates traffic through a load balancer.
# Usage: MULTI_BASE_URLS="http://localhost:8080,http://localhost:8081" ./scripts/load-multi.sh [payload_file] [num_requests] [concurrency]
# Or: export MULTI_BASE_URLS="http://localhost:8080,http://localhost:8081"
#     ./scripts/load-multi.sh scripts/payload.json 200 20
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PAYLOAD="${1:-$SCRIPT_DIR/payload.json}"
REQUESTS="${2:-200}"
CONC="${3:-20}"
# Comma-separated list of base URLs (e.g. http://localhost:8080,http://localhost:8081)
URLS_STR="${MULTI_BASE_URLS:-http://localhost:8080}"
IFS=',' read -ra URLS <<< "$URLS_STR"
N=${#URLS[@]}
if [ "$N" -eq 0 ]; then
  echo "MULTI_BASE_URLS must contain at least one URL (e.g. http://localhost:8080,http://localhost:8081)"
  exit 1
fi

if [ ! -f "$PAYLOAD" ]; then
  echo "Payload file not found: $PAYLOAD"
  echo "Run: $SCRIPT_DIR/generate-payload.sh 500 50 $PAYLOAD"
  exit 1
fi

echo "→ Round-robin load: $REQUESTS requests, $CONC concurrent, across $N instance(s)"
printf '  URLs: %s\n' "${URLS[*]}"
start=$(date +%s)
sent=0
for i in $(seq 1 "$REQUESTS"); do
  idx=$(( (i - 1) % N ))
  url="${URLS[$idx]}"
  curl -s -o /dev/null -X POST "${url}/price-updates" \
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
echo "✓ Sent $REQUESTS requests in ${elapsed}s (~$rps req/s) across $N instance(s)"
