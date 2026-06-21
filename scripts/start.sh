#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="$ROOT_DIR/.runtime"
LOG_DIR="$RUNTIME_DIR/logs"

mkdir -p "$LOG_DIR"

cd "$ROOT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required but was not found."
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven is required but was not found."
  exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "npm is required but was not found."
  exit 1
fi

if lsof -ti tcp:8080 >/dev/null 2>&1; then
  echo "Port 8080 is already in use. Run ./scripts/stop.sh or stop the backend process first."
  exit 1
fi

if lsof -ti tcp:5173 >/dev/null 2>&1; then
  echo "Port 5173 is already in use. Run ./scripts/stop.sh or stop the frontend process first."
  exit 1
fi

echo "Starting Postgres..."
docker compose up -d postgres

echo "Waiting for Postgres to become healthy..."
until [ "$(docker inspect -f '{{.State.Health.Status}}' german_flashcards_postgres 2>/dev/null || true)" = "healthy" ]; do
  sleep 1
done

if [ ! -d "$ROOT_DIR/frontend/node_modules" ]; then
  echo "Installing frontend dependencies..."
  (cd "$ROOT_DIR/frontend" && npm install)
fi

echo "Starting Spring Boot backend on http://localhost:8080 ..."
(
  cd "$ROOT_DIR/backend"
  mvn spring-boot:run
) >"$LOG_DIR/backend.log" 2>&1 &
echo "$!" > "$RUNTIME_DIR/backend.pid"

echo "Starting React frontend on http://localhost:5173 ..."
(
  cd "$ROOT_DIR/frontend"
  npm run dev -- --host 0.0.0.0
) >"$LOG_DIR/frontend.log" 2>&1 &
echo "$!" > "$RUNTIME_DIR/frontend.pid"

echo
echo "App is starting:"
echo "- Frontend: http://localhost:5173"
echo "- Backend:  http://localhost:8080"
echo "- DB:       localhost:5433"
echo
echo "Logs:"
echo "- Backend:  tail -f .runtime/logs/backend.log"
echo "- Frontend: tail -f .runtime/logs/frontend.log"
echo
echo "Stop everything with:"
echo "./scripts/stop.sh"
