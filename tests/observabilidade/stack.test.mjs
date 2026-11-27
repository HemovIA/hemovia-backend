import test from 'node:test';
import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';

const prometheus = 'http://127.0.0.1:19090';
const grafana = 'http://127.0.0.1:13000';
const api = 'http://127.0.0.1:18080';
const auth = 'Basic ' + Buffer.from(`${process.env.GRAFANA_VIEWER_USER ?? process.env.GRAFANA_ADMIN_USER}:${process.env.GRAFANA_VIEWER_PASSWORD ?? process.env.GRAFANA_ADMIN_PASSWORD}`).toString('base64');
async function get(url, headers = {}) {
  const response = await fetch(url, { headers });
  assert.equal(response.status, 200, `${new URL(url).pathname}: HTTP ${response.status}`);
  return response.json();
}
async function graf(path) { return get(grafana + path, { authorization: auth }); }
async function query(expr) {
  const result = await get(`${prometheus}/api/v1/query?${new URLSearchParams({ query: expr })}`);
  assert.equal(result.status, 'success');
  assert.ok(result.data.result.length, `Sem séries para ${expr}`);
  return result.data.result;
}
async function logs() {
  const params = new URLSearchParams({ query: '{application="hemovia-api"} |= "Requisicao de sangue processada"', limit: '100' });
  const result = await graf(`/api/datasources/proxy/uid/loki/loki/api/v1/query_range?${params}`);
  return result.data.result.flatMap(stream => stream.values.map(([, line]) => line));
}

test('@spec:AC-114 Actuator e scrape expõem métricas identificadas', async () => {
  for (const endpoint of ['health', 'info', 'metrics', 'prometheus']) {
    const body = execFileSync('docker', ['compose', 'exec', '-T', 'api', 'wget', '-qO-', `http://127.0.0.1:8081/actuator/${endpoint}`], { encoding: 'utf8', timeout: 30000 });
    assert.ok(body.trim(), `Actuator ${endpoint} vazio`);
    if (endpoint === 'prometheus') for (const name of ['http_server_requests_seconds_bucket', 'jvm_memory_used_bytes', 'jvm_gc_pause_seconds', 'jvm_threads_live_threads', 'process_cpu_usage', 'process_uptime_seconds', 'hikaricp_connections']) assert.match(body, new RegExp(name));
  }
  for (const metric of ['http_server_requests_seconds_bucket', 'jvm_memory_used_bytes', 'jvm_threads_live_threads', 'process_cpu_usage', 'process_uptime_seconds', 'hikaricp_connections_active']) await query(`${metric}{application="hemovia-api"}`);
});

test('@spec:AC-115 readiness e proteção da API pública', async () => {
  assert.deepEqual(await get(`${api}/readyz`), { status: 'UP' });
  for (const path of ['/actuator/health', '/actuator/info', '/actuator/metrics', '/actuator/prometheus']) assert.notEqual((await fetch(api + path)).status, 200);
  assert.ok([401, 403].includes((await fetch(`${api}/api/v1/donors`)).status));
});

test('@spec:AC-116 contadores confirmados e painéis separados', async () => {
  for (const metric of ['hemovia_doacoes_registradas_total', 'hemovia_requisicoes_emitidas_total']) {
    const series = await query(`${metric}{application="hemovia-api"}`);
    assert.ok(series.some(item => Number(item.value[1]) > 0), `${metric} não avançou`);
  }
  const dashboard = (await graf('/api/dashboards/uid/hemovia-aplicacao')).dashboard;
  for (const metric of ['hemovia_doacoes_registradas_total', 'hemovia_requisicoes_emitidas_total']) assert.equal(dashboard.panels.filter(p => p.targets?.some(t => t.expr?.includes(metric))).length, 1);
});

test('@spec:AC-117 exporters ativos e séries do host e contêineres', async () => {
  for (const metric of ['node_cpu_seconds_total', 'node_memory_MemTotal_bytes', 'node_filesystem_avail_bytes', 'node_network_receive_bytes_total', 'node_load1', 'container_cpu_usage_seconds_total{container_label_com_docker_compose_service!=""}', 'container_memory_working_set_bytes{container_label_com_docker_compose_service!=""}']) await query(metric);
  for (const job of ['node-exporter', 'cadvisor']) assert.ok((await query(`up{job="${job}"}`)).every(s => s.value[1] === '1'));
});

test('@spec:AC-118 alvos e datasources provisionados', async () => {
  const targets = (await get(`${prometheus}/api/v1/targets`)).data.activeTargets;
  for (const job of ['hemovia-api', 'node-exporter', 'cadvisor', 'prometheus']) assert.ok(targets.some(t => t.labels.job === job && t.health === 'up'), `${job} não está UP`);
  const sources = await graf('/api/datasources');
  for (const uid of ['prometheus', 'loki', 'tempo']) assert.ok(sources.some(s => s.uid === uid));
  assert.equal((await fetch(`${grafana}/api/datasources`)).status, 401);
});

test('@spec:AC-119 dashboards versionados e séries reais', async () => {
  for (const [uid, file] of [['hemovia-aplicacao', 'aplicacao'], ['hemovia-infraestrutura', 'infraestrutura']]) {
    const dashboard = (await graf(`/api/dashboards/uid/${uid}`)).dashboard;
    const versioned = JSON.parse(readFileSync(new URL(`../../grafana/dashboards/${file}.json`, import.meta.url)));
    assert.equal(dashboard.uid, versioned.uid);
    assert.deepEqual(dashboard.panels.map(p => p.title), versioned.panels.map(p => p.title));
    for (const panel of dashboard.panels) for (const target of panel.targets ?? []) if (target.expr) await query(target.expr);
  }
});

test('@spec:AC-120 logs da aplicação com traceId', async () => {
  const lines = await logs();
  assert.ok(lines.length, 'Log de requisição ausente no Loki');
  const entries = lines.map(line => JSON.parse(line));
  assert.ok(entries.some(e => /^[0-9a-f]{32}$/.test(e.traceId ?? '')));
  for (const line of lines) assert.doesNotMatch(line, /HEMOVIA_ACCESS|HEMOVIA_REFRESH|XSRF-TOKEN|password|"cpf"|"email"/i);
});

test('@spec:AC-121 trace do log contém spans HTTP e interno', async () => {
  const sources = await graf('/api/datasources');
  assert.ok(sources.find(s => s.uid === 'loki')?.jsonData?.derivedFields?.some(f => f.datasourceUid === 'tempo' && f.name === 'traceId'));
  assert.ok((await query('http_server_requests_seconds_count{application="hemovia-api",uri="/api/v1/requests",status="400"}')).some(s => Number(s.value[1]) > 0));
  const entries = (await logs()).map(line => JSON.parse(line));
  const id = entries.find(e => /^[0-9a-f]{32}$/.test(e.traceId ?? ''))?.traceId;
  assert.ok(id, 'Log sem traceId');
  const trace = await graf(`/api/datasources/proxy/uid/tempo/api/traces/${id}`);
  const spans = trace.batches.flatMap(batch => batch.scopeSpans.flatMap(scope => scope.spans));
  assert.ok(spans.length >= 2);
  assert.ok(spans.some(span => span.name.toLowerCase().includes('http post /api/v1/requests')));
  assert.ok(spans.some(span => span.name === 'hemovia.requisicao.criar' && span.parentSpanId));
});
