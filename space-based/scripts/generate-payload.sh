#!/usr/bin/env bash
# Generate a JSON file of price updates for load testing.
# Usage: ./generate-payload.sh [num_updates] [num_nodes] [output_file]
# Defaults: 500 updates, 50 nodes, scripts/payload.json
set -e
UPDATES="${1:-500}"
NODES="${2:-50}"
OUT="${3:-$(dirname "$0")/payload.json}"
echo "→ Generating $UPDATES updates across $NODES nodes → $OUT"
first=1
echo -n "[" > "$OUT"
for i in $(seq 1 "$UPDATES"); do
  node=$(( (i - 1) % NODES ))
  price=$(( 80 + RANDOM % 40 ))
  # Roughly 10% of updates are spikes
  if [ $(( RANDOM % 10 )) -eq 0 ]; then
    price=$(( 200 + RANDOM % 100 ))
  fi
  nodeId="NODE-$(printf '%03d' "$node")"
  if [ "$first" = 1 ]; then
    first=0
  else
    echo -n "," >> "$OUT"
  fi
  echo -n "{\"nodeId\":\"$nodeId\",\"priceMwh\":$price}" >> "$OUT"
done
echo "]" >> "$OUT"
echo "✓ Wrote $(wc -c < "$OUT") bytes to $OUT"
