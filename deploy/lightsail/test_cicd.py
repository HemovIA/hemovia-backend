import argparse
import io
import json
import os
from pathlib import Path
import subprocess
import tarfile
import tempfile


HERE = Path(__file__).resolve().parent
BACKEND = HERE.parent.parent
GOOD = "a" * 40 + "-" + "b" * 40
BAD = "c" * 40 + "-" + "d" * 40


def write(path, body, mode=0o644):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(body, encoding="utf-8", newline="\n")
    path.chmod(mode)


def test_release():
    with tempfile.TemporaryDirectory() as folder:
        root = Path(folder)
        bundle = root / "fixture.tar.gz"
        with tarfile.open(bundle, "w:gz") as archive:
            for member in ("docker-compose.yml", "deploy/lightsail/compose.yaml", "frontend/index.html", "image.tar"):
                item = tarfile.TarInfo(member)
                item.size = 4
                archive.addfile(item, io.BytesIO(b"demo"))
        for folder_name in ("deploy/lightsail", "prometheus", "grafana", "loki", "tempo", "frontend"):
            (root / folder_name).mkdir(parents=True)
        write(root / ".env", "HEMOVIA_PUBLIC_IP=192.0.2.20\n")
        write(root / "docker-compose.yml", "services: {}\n")
        write(root / "deploy/lightsail/compose.yaml", "services: {}\n")

        write(root / "bin/aws", '#!/bin/bash\nset -eu\ncp "$HEMOVIA_TEST_BUNDLE" "$4"\n', 0o755)
        write(root / "bin/mv", '#!/bin/bash\nset -eu\n' + r'''
if [[ "$HEMOVIA_TEST_FAULT" == state && "${!#}" == */.deploy/versions ]]; then
    exit 1
fi
exec /usr/bin/mv "$@"
''', 0o755)
        write(root / "bin/curl", '#!/bin/bash\nset -eu\n' + r'''
fault=
if [[ -f "$HEMOVIA_DEPLOY_ROOT/active" ]] &&
    [[ $(cat "$HEMOVIA_DEPLOY_ROOT/active") == *"/$HEMOVIA_TEST_BAD/"* ]]; then
    fault=$HEMOVIA_TEST_FAULT
fi
case "$*" in
    *readyz*)
        [[ "$fault" != api ]] || exit 22
        printf '{"status":"UP"}\n' ;;
    *https://192.0.2.20/)
        case "$fault" in
            redirect) printf '302 64' ;;
            empty) printf '200 0' ;;
            *) printf '200 64' ;;
        esac ;;
    *) exit 2 ;;
esac
''', 0o755)
        write(root / "bin/docker", '#!/bin/bash\nset -eu\n' + r'''
case "$1" in
    inspect) echo sha256:baseline ;;
    tag|load|image) ;;
    compose)
        [[ "$2 $3 $4" == "--project-name hemovia-demo --env-file" ]] || exit 2
        [[ "$5" == "$HEMOVIA_DEPLOY_ROOT/.env" ]] || exit 2
        while [[ $# -gt 0 ]]; do
            case "$1" in
                up)
                    printf '%s\n' "$*" >> "$HEMOVIA_DEPLOY_ROOT/operations"
                    if [[ "$HEMOVIA_TEST_FAULT" == compose && "$directory" == *"/$HEMOVIA_TEST_BAD/"* ]]; then
                        exit 1
                    fi
                    printf '%s\n' "$directory" > "$HEMOVIA_DEPLOY_ROOT/active"
                    exit ;;
                ps)
                    printf '%s\n' postgres api prometheus loki tempo alloy node-exporter cadvisor mailpit caddy
                    if [[ "$HEMOVIA_TEST_FAULT" != grafana || "$directory" != *"/$HEMOVIA_TEST_BAD/"* ]]; then
                        printf '%s\n' grafana
                    fi
                    exit ;;
                -f)
                    if [[ "$2" == */release-compose.yaml ]]; then
                        directory=$2
                    fi ;;
            esac
            shift
        done ;;
esac
''', 0o755)

        env = {
            **os.environ,
            "PATH": str(root / "bin") + os.pathsep + os.environ["PATH"],
            "HEMOVIA_DEPLOY_ROOT": str(root),
            "HEMOVIA_TEST_BUNDLE": str(bundle),
            "HEMOVIA_TEST_BAD": BAD,
        }
        state = root / ".deploy" / "versions"

        def release(*args, status=0, fault=""):
            result = subprocess.run(
                ["bash", str(HERE / "release.sh"), *args],
                env={**env, "HEMOVIA_TEST_FAULT": fault},
                text=True,
                capture_output=True,
            )
            assert result.returncode == status, result.stdout + result.stderr
            return result.stdout + result.stderr

        release("seed")
        assert state.read_text().split() == ["baseline"]
        release("deploy", GOOD, "release-bucket")
        assert state.read_text().split() == [GOOD, "baseline"]
        good = state.parent / "releases" / GOOD
        assert f"image: hemovia-api:{GOOD.split('-')[0]}" in (good / "release-compose.yaml").read_text()
        assert (good / "frontend/index.html").read_bytes() == b"demo"
        print("PASS CICD-03")
        assert "--detach --no-build --wait" in (root / "operations").read_text()
        for fault in ("api", "redirect", "empty", "grafana"):
            failure = release("deploy", BAD, "release-bucket", status=1, fault=fault)
            assert f"Versão recuperada: {GOOD}" in failure
            assert state.read_text().split() == [GOOD, "baseline"]
            assert str(good) in (root / "active").read_text()
        print("PASS CICD-04")
        for fault in ("compose", "state"):
            failure = release("deploy", BAD, "release-bucket", status=1, fault=fault)
            assert f"Versão recuperada: {GOOD}" in failure
            assert state.read_text().split() == [GOOD, "baseline"]
            assert str(good) in (root / "active").read_text()
        print("PASS CICD-05")
        release("rollback", status=1, fault="state")
        assert state.read_text().split() == [GOOD, "baseline"]
        assert str(good) in (root / "active").read_text()
        release("rollback")
        assert state.read_text().split() == ["baseline", GOOD]
        assert "/baseline/release-compose.yaml" in (root / "active").read_text()
        assert (root / ".env").read_text() == "HEMOVIA_PUBLIC_IP=192.0.2.20\n"
        assert "down" not in (root / "operations").read_text()
        print("PASS CICD-06")

        import fcntl
        with (root / ".deploy" / "lock").open("r+") as lock:
            fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
            assert "Outra operação" in release("rollback", status=1)
            assert state.read_text().split() == ["baseline", GOOD]
        print("PASS CICD-09")


def test_workflows(frontend):
    front = (frontend / ".github/workflows/ci.yml").read_text(encoding="utf-8")
    assert front.count("branches: [main]") == 2 and "npm ci" in front and "npm run build" in front
    print("PASS CICD-01")
    ci = (BACKEND / ".github/workflows/ci-cd.yml").read_text(encoding="utf-8")
    assert "postgres:16-alpine" in ci and "mvnw -B -Pbase-entities-acceptance verify" in ci
    print("PASS CICD-02")
    workflows = [path.read_text(encoding="utf-8") for path in (BACKEND / ".github/workflows").glob("*.yml")]
    assert all("RENDER_" not in content and "Deploy no Render" not in content for content in workflows)
    assert not (BACKEND / "render.yaml").exists()
    assert "## Deploy no Render" not in (BACKEND / "README.md").read_text(encoding="utf-8")
    rollback = (BACKEND / ".github/workflows/rollback.yml").read_text(encoding="utf-8")
    assert "uses: ./.github/workflows/deploy-aws-demo.yml" in rollback and "operation: rollback" in rollback
    print("PASS CICD-07")
    template = json.loads((HERE / "aws-cicd.json").read_text(encoding="utf-8"))
    role = template["Resources"]["GitHubDeployRole"]["Properties"]
    trust = role["AssumeRolePolicyDocument"]["Statement"][0]["Condition"]["StringEquals"]
    assert trust["token.actions.githubusercontent.com:aud"] == "sts.amazonaws.com"
    assert trust["token.actions.githubusercontent.com:sub"] == {
        "Fn::Sub": "repo:${GitHubRepositorySubject}:ref:refs/heads/main"
    }
    assert "StringLike" not in role["AssumeRolePolicyDocument"]["Statement"][0]["Condition"]
    assert "AWS-RunShellScript" not in json.dumps(role)
    print("PASS CICD-08")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--frontend", default=".", type=Path)
    parser.add_argument("--host-only", action="store_true")
    parser.add_argument("--config-only", action="store_true")
    args = parser.parse_args()
    if not args.config_only:
        test_release()
    if not args.host_only:
        test_workflows(args.frontend)
