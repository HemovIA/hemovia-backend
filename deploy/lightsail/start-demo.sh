#!/bin/bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.."
public_ip=${1:?Informe o IPv4 publico da instancia Lightsail}

if [ ! -f .env ]; then
    umask 077
    env_file=$(mktemp .env.XXXXXX)
    trap 'rm -f -- "$env_file"' EXIT
    cat > "$env_file" <<EOF
HEMOVIA_PUBLIC_IP=$public_ip
POSTGRES_DB=hemovia
POSTGRES_USER=hemovia
POSTGRES_PASSWORD=$(openssl rand -hex 24)
JWT_SECRET=$(openssl rand -hex 48)
ADMIN_NAME=Administrador Demo Hemovia
ADMIN_EMAIL=demo-admin@hemovia.example
ADMIN_CPF=00000000191
ADMIN_PASSWORD=$(openssl rand -hex 24)
GRAFANA_ADMIN_USER=hemovia
GRAFANA_ADMIN_PASSWORD=$(openssl rand -hex 24)
EOF
    mv -- "$env_file" .env
fi

export HEMOVIA_PUBLIC_IP="$public_ip"
compose=(docker compose --project-name hemovia-demo -f docker-compose.yml -f deploy/lightsail/compose.yaml)
"${compose[@]}" config --quiet
"${compose[@]}" run --rm --no-deps caddy caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile
"${compose[@]}" up --detach --build --wait --wait-timeout 180
