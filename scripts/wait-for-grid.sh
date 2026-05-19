#!/usr/bin/env bash
# Waits until Selenium Grid hub is ready (Linux Jenkins agents)
GRID_URL="${1:-http://localhost:4444/wd/hub/status}"
MAX_ATTEMPTS="${2:-30}"
SLEEP_SECONDS="${3:-5}"

echo "Waiting for Selenium Grid at ${GRID_URL} ..."
for ((i=1; i<=MAX_ATTEMPTS; i++)); do
  if curl -sf "${GRID_URL}" | grep -q '"ready":true'; then
    echo "Selenium Grid is READY (attempt ${i})"
    exit 0
  fi
  echo "Grid not ready yet (attempt ${i}/${MAX_ATTEMPTS})..."
  sleep "${SLEEP_SECONDS}"
done
echo "Selenium Grid did not become ready in time."
exit 1
