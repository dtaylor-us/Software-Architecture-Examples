#!/usr/bin/env bash
# Send a single bulk price-update request (small payload). Good for a quick smoke test.
set -e
BASE_URL="${BASE_URL:-http://localhost:8080}"
echo "→ POST ${BASE_URL}/price-updates (4 updates)"
curl -s -X POST "${BASE_URL}/price-updates" \
  -H "Content-Type: application/json" \
  -d '[
    {"nodeId":"NODE-001","priceMwh":100},
    {"nodeId":"NODE-002","priceMwh":100},
    {"nodeId":"NODE-001","priceMwh":250},
    {"nodeId":"NODE-002","priceMwh":90}
  ]' | head -c 500
echo
echo "✓ Request sent. Check 'accepted' and 'alertsRaised' in the JSON above."
