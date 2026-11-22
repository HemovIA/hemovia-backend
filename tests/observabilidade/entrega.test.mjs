import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { readFileSync, readdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import test from 'node:test';

const base = 'http://127.0.0.1:13000';
const repo = new URL('../../', import.meta.url);
const evidence = new URL('docs/observabilidade/evidencias/', repo);
const report = new URL('docs/observabilidade/relatorio.pdf', repo);

test('@spec:AC-122 Viewer consulta dashboards e Compose limita o acesso a loopback', async () => {
  const { GRAFANA_VIEWER_USER, GRAFANA_VIEWER_PASSWORD } = process.env;
  assert.ok(GRAFANA_VIEWER_USER && GRAFANA_VIEWER_PASSWORD, 'Configure as credenciais da conta Viewer');
  const auth = Buffer.from(`${GRAFANA_VIEWER_USER}:${GRAFANA_VIEWER_PASSWORD}`).toString('base64');
  async function get(path) {
    const response = await fetch(base + path, { headers: { authorization: `Basic ${auth}` } });
    assert.equal(response.status, 200, path);
    return response.json();
  }

  const organizations = await get('/api/user/orgs');
  assert.ok(organizations.some(org => org.orgId === 1 && org.role === 'Viewer'));
  for (const uid of ['hemovia-aplicacao', 'hemovia-infraestrutura']) {
    const { meta } = await get(`/api/dashboards/uid/${uid}`);
    assert.equal(meta.canSave, false);
  }
  for (const uid of ['prometheus', 'loki', 'tempo']) {
    assert.equal((await get(`/api/datasources/uid/${uid}`)).uid, uid);
  }

  const config = JSON.parse(execFileSync('docker', ['compose', 'config', '--format', 'json'], {
    cwd: fileURLToPath(repo), encoding: 'utf8', timeout: 15000,
  }));
  for (const [name, service] of Object.entries(config.services)) {
    if (['api', 'grafana', 'prometheus'].includes(name)) {
      assert.ok(service.ports.length > 0);
      assert.ok(service.ports.every(port => port.host_ip === '127.0.0.1'), name);
    } else {
      assert.ok(!service.ports?.length, name);
    }
  }
  assert.ok(config.services.api.ports.every(port => port.target !== 8081));
  const nodeExporter = config.services['node-exporter'];
  const gateway = config.networks.default.ipam.config[0].gateway;
  assert.ok(nodeExporter.command.includes(`--web.listen-address=${gateway}:9100`));
});

test('@spec:AC-123 relatório da demonstração com evidências datadas', () => {
  const readme = readFileSync(new URL('README.md', repo), 'utf8');
  for (const term of ['docker compose up', '13000', '19090', '18080',
    'hemovia_doacoes_registradas_total', 'hemovia_requisicoes_emitidas_total', 'relatorio.pdf']) {
    assert.ok(readme.includes(term), `README sem referência a ${term}`);
  }

  const captures = readdirSync(evidence).filter(file => file.endsWith('.png'));
  assert.ok(captures.length >= 7, 'Evidências da stack, métricas, dashboards, logs e traces');
  for (const file of captures) {
    const image = readFileSync(new URL(file, evidence));
    assert.equal(image.subarray(1, 4).toString('ascii'), 'PNG', file);
    assert.ok(image.length > 1000, file);
  }

  const pdfText = execFileSync(process.env.HEMOVIA_PDF_PYTHON ?? 'python',
    ['-X', 'utf8', '-c', 'import sys; from pypdf import PdfReader; print(chr(10).join(p.extract_text() for p in PdfReader(sys.argv[1]).pages))',
      fileURLToPath(report)], { encoding: 'utf8', timeout: 15000 });
  for (const term of [/Infraestrutura de Comunica/i, /github\.com\/HemovIA\/hemovia-backend/i,
    /arquitetura/i, /Prometheus/i, /Loki/i, /Tempo/i, /Viewer/i, /\d{2}[/-]\d{2}[/-]\d{4}|\d{4}-\d{2}-\d{2}/]) {
    assert.match(pdfText, term);
  }
});
