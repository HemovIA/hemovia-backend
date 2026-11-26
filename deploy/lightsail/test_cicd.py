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
SERVICES = "postgres api prometheus grafana loki tempo alloy node-exporter cadvisor mailpit caddy"


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
        write(root / "bin/curl", '#!/bin/bash\ncase "$*" in *readyz*) printf \'{"status":"UP"}\\n\';; esac\n', 0o755)
        write(root / "bin/docker", '#!/bin/bash\nset -eu\n' + r'''
case "$1" in
    inspect) echo sha256:baseline ;;
    tag|load|image) ;;
    compose)
        [[ "$2 $3" == "--project-name hemovia-demo" ]] || exit 2
        while [[ $# -gt 0 ]]; do
            case "$1" in
                up)
                    printf '%s\n' "$*" >> "$HEMOVIA_DEPLOY_ROOT/operations"
                    [[ "$directory" != *"/$HEMOVIA_TEST_BAD/"* ]]
                    exit ;;
                ps)
                    printf '%s\n' postgres api prometheus grafana loki tempo alloy node-exporter cadvisor mailpit caddy
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

        def release(*args, status=0):
            result = subprocess.run(["bash", str(HERE / "release.sh"), *args], env=env, text=True, capture_output=True)
            assert result.returncode == status, result.stdout + result.stderr
            return result.stdout + result.stderr

        release("seed")
        assert state.read_text().split() == ["baseline"]
        release("deploy", GOOD, "release-bucket")
        assert state.read_text().split() == [GOOD, "baseline"]
        print("PASS CICD-03")
        assert "--wait" in (root / "operations").read_text()
        assert all(service in (HERE / "release.sh").read_text() for service in SERVICES.split())
        print("PASS CICD-04")
        failure = release("deploy", BAD, "release-bucket", status=1)
        assert f"Versão recuperada: {GOOD}" in failure
        assert state.read_text().split() == [GOOD, "baseline"]
        print("PASS CICD-05")
        release("rollback")
        assert state.read_text().split() == ["baseline", GOOD]
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
