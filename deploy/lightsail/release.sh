#!/usr/bin/env bash
set -euo pipefail

root=$(realpath "${HEMOVIA_DEPLOY_ROOT:-/opt/hemovia-demo}")
operation=${1:?Informe seed, deploy ou rollback}
release=${2:-}
bucket=${3:-}
state="$root/.deploy"
releases="$state/releases"

umask 077
mkdir -p "$releases"
exec 9>"$state/lock"
flock -n 9 || { echo 'Outra operação de deploy está em andamento.' >&2; exit 1; }

current=
previous=
if [[ -f "$state/versions" ]]; then
    read -r current previous < "$state/versions"
fi

record_version() {
    local next
    next=$(mktemp "$state/versions.XXXXXX") || return 1
    if ! printf '%s %s\n' "$1" "$2" > "$next" || ! mv -fT -- "$next" "$state/versions"; then
        rm -f -- "$next"
        return 1
    fi
}

write_image_override() {
    cat > "$1/release-compose.yaml" <<EOF
services:
  api:
    build: !reset null
    image: $2
EOF
}

compose() {
    local directory=$1
    shift
    docker compose --project-name hemovia-demo --env-file "$root/.env" \
        -f "$directory/docker-compose.yml" \
        -f "$directory/deploy/lightsail/compose.yaml" \
        -f "$directory/release-compose.yaml" "$@"
}

healthy() {
    local directory=$1
    local public_ip page
    public_ip=$(sed -n 's/^HEMOVIA_PUBLIC_IP=//p' "$root/.env")
    [[ $public_ip =~ ^[0-9]{1,3}(\.[0-9]{1,3}){3}$ ]] || return 1

    curl -fsS --max-time 5 http://127.0.0.1:18080/readyz \
        | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' || return 1
    page=$(curl -fsS --max-time 10 --connect-to "$public_ip:443:127.0.0.1:443" \
        --output /dev/null --write-out '%{http_code} %{size_download}' \
        "https://$public_ip/") || return 1
    [[ $page =~ ^200\ [1-9][0-9]*$ ]] || return 1

    local running service
    running=$(compose "$directory" ps --services --status running) || return 1
    for service in postgres api prometheus grafana loki tempo alloy node-exporter cadvisor mailpit caddy; do
        grep -Fxq "$service" <<< "$running" || return 1
    done
}

activate() {
    local directory="$releases/$1"
    compose "$directory" up --detach --no-build --wait --wait-timeout 180 &&
        healthy "$directory"
}

stage_release() (
    local stage
    stage=$(mktemp -d "$state/stage.XXXXXX")
    trap 'rm -rf -- "$stage"' EXIT

    aws s3 cp "s3://$bucket/releases/$release.tar.gz" "$stage/release.tar.gz" \
        --no-progress --checksum-mode ENABLED
    mkdir "$stage/contents"
    python3 - "$stage/release.tar.gz" "$stage/contents" <<'PY'
import sys
import tarfile

with tarfile.open(sys.argv[1], "r:gz") as archive:
    if any(not (item.isfile() or item.isdir()) for item in archive.getmembers()):
        raise SystemExit("O pacote contém um tipo de arquivo inesperado.")
    archive.extractall(sys.argv[2], filter="data")
PY

    local directory="$stage/contents"
    for file in image.tar frontend/index.html docker-compose.yml deploy/lightsail/compose.yaml; do
        test -f "$directory/$file"
    done
    local image="hemovia-api:${release%%-*}"
    docker load --input "$directory/image.tar"
    docker image inspect "$image" --format '{{.Id}}' > /dev/null
    rm -f -- "$directory/image.tar"
    write_image_override "$directory" "$image"
    compose "$directory" config --quiet
    mv -- "$directory" "$releases/$release"
)

case "$operation" in
    seed)
        [[ -z "$current" ]] || { echo "Versão já registrada: $current"; exit 0; }
        baseline="$releases/baseline"
        mkdir -p "$baseline/deploy"
        cp -a -- "$root/docker-compose.yml" "$root/prometheus" "$root/grafana" \
            "$root/loki" "$root/tempo" "$root/frontend" "$baseline/"
        cp -a -- "$root/deploy/lightsail" "$baseline/deploy/"
        docker tag "$(docker inspect --format '{{.Image}}' hemovia-demo-api-1)" hemovia-api:demo-baseline
        write_image_override "$baseline" hemovia-api:demo-baseline
        healthy "$baseline"
        record_version baseline ''
        echo 'Versão inicial registrada: baseline'
        ;;
    deploy)
        [[ $release =~ ^[[:xdigit:]]{40}-[[:xdigit:]]{40}$ && -n "$bucket" ]] ||
            { echo 'Identificador de release ou bucket inválido.' >&2; exit 2; }
        [[ -n "$current" ]] || { echo 'Registre a versão inicial antes de publicar.' >&2; exit 1; }
        if [[ ! -d "$releases/$release" ]]; then
            stage_release
        fi
        if activate "$release" &&
            { [[ $release == "$current" ]] || record_version "$release" "$current"; }; then
            echo "Versão aplicada: $release"
        else
            echo "Falha na versão $release. Reaplicando $current." >&2
            if activate "$current"; then
                echo "Versão recuperada: $current"
            else
                echo "Falha também na recuperação de $current." >&2
            fi
            exit 1
        fi
        ;;
    rollback)
        [[ -n "$previous" ]] || { echo 'Nenhuma versão anterior aprovada.' >&2; exit 1; }
        if ! { activate "$previous" && record_version "$previous" "$current"; }; then
            echo "O rollback para $previous falhou. Reaplicando $current." >&2
            activate "$current" || echo "Falha também na recuperação de $current." >&2
            exit 1
        fi
        echo "Rollback aplicado: $previous"
        ;;
    *)
        echo 'Operação inválida.' >&2
        exit 2
        ;;
esac
