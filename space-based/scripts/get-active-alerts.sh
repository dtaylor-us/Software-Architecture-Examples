#!/usr/bin/env bash
# Fetch active alerts from the space (read path).
set -e
BASE_URL="${BASE_URL:-http://localhost:8080}"
echo "→ GET ${BASE_URL}/active-alerts"
curl -s "${BASE_URL}/active-alerts" | head -c 800
echo
echo "✓ Done. Empty array [] means no active alerts (or they expired)."
