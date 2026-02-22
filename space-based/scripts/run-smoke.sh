#!/usr/bin/env bash
# Smoke test: health, one bulk POST, then list active alerts. No extra tools required.
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080}"
echo "=== Smoke test (BASE_URL=$BASE_URL) ==="
echo
"$SCRIPT_DIR/health.sh"
echo
"$SCRIPT_DIR/post-bulk.sh"
echo
"$SCRIPT_DIR/get-active-alerts.sh"
echo
echo "=== Smoke test done ==="
