#!/usr/bin/env bash

set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$ROOT_DIR/.env"
DEV_JWT_SECRET_FILE="$ROOT_DIR/.dev-jwt-secret"
LOG_DIR="$ROOT_DIR/.dev-logs"
STARTUP_TIMEOUT_SECONDS="${STARTUP_TIMEOUT_SECONDS:-120}"

SERVICE_NAMES=()
SERVICE_PIDS=()
LAST_STARTED_PID=
DATABASE_STARTED_BY_SCRIPT=false

log() {
    printf '[AmtPilot] %s\n' "$*"
}

fail() {
    printf '[AmtPilot] ERROR: %s\n' "$*" >&2
    exit 1
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || fail "'$1' is required but was not found."
}

load_environment() {
    local line
    local key
    local value

    while IFS= read -r line || [[ -n "$line" ]]; do
        line="${line%$'\r'}"
        [[ -z "$line" || "$line" == \#* ]] && continue
        [[ "$line" == *=* ]] || fail "Invalid line in .env: $line"

        key="${line%%=*}"
        value="${line#*=}"
        [[ "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] \
            || fail "Invalid variable name in .env: $key"

        if [[ "$value" == \"*\" || "$value" == \'*\' ]]; then
            value="${value:1:-1}"
        fi
        export "$key=$value"
    done <"$ENV_FILE"
}

cleanup() {
    local exit_code=$?
    trap - EXIT INT TERM

    if (("${#SERVICE_PIDS[@]}" > 0)); then
        log "Stopping application services..."
        for pid in "${SERVICE_PIDS[@]}"; do
            kill "$pid" >/dev/null 2>&1 || true
        done
        wait "${SERVICE_PIDS[@]}" 2>/dev/null || true
    fi

    if [[ "$DATABASE_STARTED_BY_SCRIPT" == true ]]; then
        log "Stopping PostgreSQL..."
        (
            cd "$ROOT_DIR"
            docker compose -f compose.yml stop postgres >/dev/null
        ) || true
    fi

    exit "$exit_code"
}

start_service() {
    local name=$1
    local working_directory=$2
    local log_file=$3
    shift 3

    (
        cd "$working_directory"
        exec "$@"
    ) >"$log_file" 2>&1 &

    SERVICE_NAMES+=("$name")
    SERVICE_PIDS+=("$!")
    LAST_STARTED_PID="$!"
    log "Starting $name (log: $log_file)"
}

wait_for_url() {
    local name=$1
    local url=$2
    local pid=$3
    local log_file=$4
    local elapsed=0

    while ((elapsed < STARTUP_TIMEOUT_SECONDS)); do
        if curl --fail --silent --show-error "$url" >/dev/null 2>&1; then
            log "$name is ready: $url"
            return 0
        fi

        if ! kill -0 "$pid" >/dev/null 2>&1; then
            printf '\n[AmtPilot] %s stopped during startup. Last log lines:\n' "$name" >&2
            tail -n 40 "$log_file" >&2 || true
            return 1
        fi

        sleep 1
        ((elapsed += 1))
    done

    printf '\n[AmtPilot] %s did not become ready within %s seconds. Last log lines:\n' \
        "$name" "$STARTUP_TIMEOUT_SECONDS" >&2
    tail -n 40 "$log_file" >&2 || true
    return 1
}

monitor_services() {
    while true; do
        for index in "${!SERVICE_PIDS[@]}"; do
            local pid="${SERVICE_PIDS[$index]}"
            if ! kill -0 "$pid" >/dev/null 2>&1; then
                local name="${SERVICE_NAMES[$index]}"
                printf '\n[AmtPilot] %s stopped unexpectedly. Check %s/%s.log\n' \
                    "$name" "$LOG_DIR" "$name" >&2
                return 1
            fi
        done
        sleep 2
    done
}

trap cleanup EXIT INT TERM

require_command docker
require_command curl
require_command npm

[[ -f "$ENV_FILE" ]] || fail "Create .env from .env.example before starting the project."
[[ -d "$ROOT_DIR/frontend/node_modules" ]] || fail "Run 'npm install' inside frontend first."

if [[ -x "$ROOT_DIR/ai-service/.venv/Scripts/python.exe" ]]; then
    AI_PYTHON="$ROOT_DIR/ai-service/.venv/Scripts/python.exe"
elif [[ -x "$ROOT_DIR/ai-service/.venv/bin/python" ]]; then
    AI_PYTHON="$ROOT_DIR/ai-service/.venv/bin/python"
else
    fail "Create the ai-service virtual environment and install its dependencies first."
fi

load_environment

if [[ -z "${JWT_SECRET:-}" || "$JWT_SECRET" == replace-with-* ]]; then
    if [[ -s "$DEV_JWT_SECRET_FILE" ]]; then
        IFS= read -r JWT_SECRET <"$DEV_JWT_SECRET_FILE"
    else
        require_command openssl
        umask 077
        openssl rand -hex 32 >"$DEV_JWT_SECRET_FILE"
        IFS= read -r JWT_SECRET <"$DEV_JWT_SECRET_FILE"
        log "Generated a persistent local JWT secret in .dev-jwt-secret."
    fi
    export JWT_SECRET
fi

mkdir -p "$LOG_DIR"

if (
    cd "$ROOT_DIR"
    docker compose -f compose.yml ps --status running --services
) | grep -qx postgres; then
    log "PostgreSQL is already running."
else
    log "Starting PostgreSQL..."
    (
        cd "$ROOT_DIR"
        docker compose -f compose.yml up -d postgres
    )
    DATABASE_STARTED_BY_SCRIPT=true
fi

log "Waiting for PostgreSQL..."
database_waited=0
until (
    cd "$ROOT_DIR"
    docker compose -f compose.yml exec -T postgres \
        pg_isready -U "${POSTGRES_USER:-amtpilot}" -d "${POSTGRES_DB:-amtpilot}" \
        >/dev/null 2>&1
); do
    ((database_waited += 1))
    if ((database_waited >= STARTUP_TIMEOUT_SECONDS)); then
        fail "PostgreSQL did not become ready within $STARTUP_TIMEOUT_SECONDS seconds."
    fi
    sleep 1
done
log "PostgreSQL is ready."

if [[ -x "$ROOT_DIR/mvnw" ]]; then
    start_service "backend" "$ROOT_DIR" "$LOG_DIR/backend.log" \
        "$ROOT_DIR/mvnw" spring-boot:run
else
    start_service "backend" "$ROOT_DIR" "$LOG_DIR/backend.log" \
        sh "$ROOT_DIR/mvnw" spring-boot:run
fi
BACKEND_PID="$LAST_STARTED_PID"

start_service "ai-service" "$ROOT_DIR/ai-service" "$LOG_DIR/ai-service.log" \
    "$AI_PYTHON" -m uvicorn app.main:app --reload --port 8000
AI_SERVICE_PID="$LAST_STARTED_PID"

start_service "frontend" "$ROOT_DIR/frontend" "$LOG_DIR/frontend.log" \
    npm run dev -- --host 127.0.0.1
FRONTEND_PID="$LAST_STARTED_PID"

wait_for_url "AI service" "http://localhost:8000/health" \
    "$AI_SERVICE_PID" "$LOG_DIR/ai-service.log"
wait_for_url "Frontend" "http://localhost:5173/" \
    "$FRONTEND_PID" "$LOG_DIR/frontend.log"
wait_for_url "Spring Boot" "http://localhost:8080/actuator/health" \
    "$BACKEND_PID" "$LOG_DIR/backend.log"

printf '\nAmtPilot is running:\n'
printf '  Frontend:   http://localhost:5173/\n'
printf '  Backend:    http://localhost:8080/\n'
printf '  Swagger:    http://localhost:8080/swagger-ui.html\n'
printf '  AI service: http://localhost:8000/docs\n'
printf '\nPress Ctrl+C to stop the services started by this script.\n\n'

monitor_services
