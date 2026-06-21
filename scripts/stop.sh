#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="$ROOT_DIR/.runtime"

stop_pid_file() {
  local name="$1"
  local pid_file="$RUNTIME_DIR/$name.pid"

  if [ -f "$pid_file" ]; then
    local pid
    pid="$(cat "$pid_file")"
    if kill -0 "$pid" >/dev/null 2>&1; then
      echo "Stopping $name process ($pid)..."
      kill "$pid" >/dev/null 2>&1 || true
      sleep 1
      if kill -0 "$pid" >/dev/null 2>&1; then
        kill -9 "$pid" >/dev/null 2>&1 || true
      fi
    fi
    rm -f "$pid_file"
  fi
}

stop_port() {
  local port="$1"
  local label="$2"
  local pids
  pids="$(lsof -ti tcp:"$port" 2>/dev/null || true)"

  if [ -n "$pids" ]; then
    echo "Stopping $label processes on port $port..."
    echo "$pids" | xargs kill >/dev/null 2>&1 || true
    sleep 1
    pids="$(lsof -ti tcp:"$port" 2>/dev/null || true)"
    if [ -n "$pids" ]; then
      echo "$pids" | xargs kill -9 >/dev/null 2>&1 || true
    fi
  fi
}

cd "$ROOT_DIR"

stop_pid_file "frontend"
stop_pid_file "backend"

stop_port 5173 "frontend"
stop_port 8080 "backend"

if [ "${1:-}" = "--keep-db" ]; then
  echo "Keeping Postgres running because --keep-db was passed."
else
  echo "Stopping Postgres..."
  docker compose down
fi

echo "Stopped."
