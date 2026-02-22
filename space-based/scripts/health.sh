#!/usr/bin/env bash
# Health check: verify the API is up and reports space-based architecture.
set -e
BASE_URL="${BASE_URL:-http://localhost:8080}"
echo "→ GET ${BASE_URL}/health"
response=$(curl -s -w "\n%{http_code}" "${BASE_URL}/health")
body=$(echo "$response" | sed '$d')
code=$(echo "$response" | tail -n 1)
if [ "$code" = "200" ]; then
  echo "$body" | head -c 200
  echo
  echo "✓ Health OK (HTTP 200)"
else
  echo "✗ Health failed (HTTP $code)"
  echo "$body"
  exit 1
fi
