import assert from 'node:assert/strict';
import { mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { spawnSync } from 'node:child_process';
import { randomUUID } from 'node:crypto';
import test from 'node:test';
import { assessCriterion, readFreshReports } from './read-surefire.mjs';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
// Register historical tests in the outer TAP stream too: each historical
// acceptance criterion needs its own executed proof, not only a child exit code.
for (const folder of ['tests/revisao-projeto', 'tests/finalizando-entidades-base']) {
  for (const file of readdirSync(path.join(root, folder)).filter(name => name.endsWith('.test.mjs'))) {
    await import(pathToFileURL(path.join(root, folder, file)).href);
  }
}
const manifest = JSON.parse(readFileSync(path.join(root, 'scripts/spec-java/acceptance-cases.json'), 'utf8'));
const directory = path.join(root, 'api/target/base-entities-reports', randomUUID());
mkdirSync(directory, { recursive: true });
const startedAt = Date.now();
const maven = spawnSync(process.platform === 'win32' ? 'mvn.cmd' : 'mvn',
  ['-o', '-B', '-f', 'api/pom.xml', '-Pbase-entities-acceptance', `-Dbase.entities.reports=${directory}`, 'test'],
  { cwd: root, shell: process.platform === 'win32', encoding: 'utf8', timeout: 180000, maxBuffer: 32 * 1024 * 1024 });
writeFileSync(path.join(directory, 'maven.log'), `${maven.stdout ?? ''}\n${maven.stderr ?? ''}`);
let cases = [], reportError;
try { cases = readFreshReports(directory, startedAt); } catch (error) { reportError = error; }
for (const [id, required] of Object.entries(manifest)) {
  if (id === 'AC-124') continue;
  test(`@spec:${id} casos JUnit obrigatórios, inclusive PostgreSQL`, () => {
    if (reportError) throw reportError;
    const result = assessCriterion(cases, required);
    assert.ok(result.pass, result.reason);
  });
}
test('@spec:AC-014 suíte Maven completa precisa terminar sem falhas ou skips', () => {
  assert.equal(maven.error, undefined, `Não foi possível iniciar Maven: ${maven.error?.message ?? 'erro desconhecido'}`);
  assert.notEqual(maven.signal, 'SIGTERM', 'Maven excedeu o tempo de execução da verificação');
  assert.equal(maven.status, 0, `Suíte falhou; consulte ${path.relative(root, directory)}/maven.log`);
  assert.ok(cases.length > 0); assert.ok(cases.every(c => c.status === 'pass'), 'Há falha/skip fora dos critérios também');
});
test('@spec:AC-014 regressões documentais e testes do adaptador continuam ativos', () => {
  const files = ['tests/revisao-projeto', 'tests/finalizando-entidades-base'].flatMap(dir =>
    readdirSync(path.join(root, dir)).filter(f => f.endsWith('.test.mjs')).map(f => path.join(dir, f)));
  const result = spawnSync(process.execPath, ['--test', ...files], { cwd: root, encoding: 'utf8', timeout: 60000 });
  writeFileSync(path.join(directory, 'node.log'), `${result.stdout ?? ''}\n${result.stderr ?? ''}`);
  assert.equal(result.status, 0, `Regressões Node falharam; consulte ${path.relative(root, directory)}/node.log`);
});

// Real browser results are read from a fresh report in this isolated run.
const frontend = path.resolve(root, '../hemovia-frontend');
const browserReport = path.join(directory, 'playwright.json');
const browser = spawnSync(process.execPath,
  [path.join(frontend, 'node_modules/@playwright/test/cli.js'), 'test', '--reporter=json', '--workers=1'],
  { cwd: frontend, env: { ...process.env, PLAYWRIGHT_JSON_OUTPUT_FILE: browserReport }, encoding: 'utf8', timeout: 180000, maxBuffer: 32 * 1024 * 1024 });
writeFileSync(path.join(directory, 'playwright.log'), `${browser.stdout ?? ''}\n${browser.stderr ?? ''}`);
let browserSpecs = [], browserError;
try {
  const report = JSON.parse(readFileSync(browserReport, 'utf8'));
  function collect(suites) { for (const suite of suites || []) { browserSpecs.push(...(suite.specs || [])); collect(suite.suites); } }
  collect(report.suites);
  if (!browserSpecs.length) throw new Error('Relatório Playwright sem testes executados');
} catch (error) { browserError = error; }
function browserPassed(id) {
  if (browserError) throw browserError;
  assert.equal(browser.status, 0, `Suíte Playwright falhou; consulte ${path.relative(root, directory)}/playwright.log`);
  const matching = browserSpecs.filter(spec => spec.title.includes(`@spec:${id}`));
  assert.ok(matching.length, `${id} não possui caso Playwright executado`);
  for (const spec of matching) {
    assert.ok(spec.tests.length && spec.ok && spec.tests.every(t => t.status === 'expected' && t.expectedStatus === 'passed' && t.results.length && t.results.every(r => r.status === 'passed')), `${id}: ${spec.title} falhou ou foi pulado`);
  }
}
for (const id of ['AC-096','AC-097','AC-098','AC-099','AC-100','AC-101','AC-102','AC-103','AC-104','AC-105','AC-106','AC-108','AC-109','AC-110','AC-111','AC-112','AC-113','AC-130']) {
  test(`@spec:${id} interações Playwright reais`, () => browserPassed(id));
}
test('@spec:AC-124 formulário público e API passam na mesma execução', () => {
  browserPassed('AC-124');
  assert.ok(!reportError, reportError?.message);
  const result = assessCriterion(cases, manifest['AC-124']);
  assert.ok(result.pass, result.reason);
});
test('@spec:AC-107 integração e contratos backend e frontend executados sem falhas', () => {
  assert.equal(maven.status, 0, 'Suíte Maven falhou');
  assert.ok(cases.length && cases.every(c => c.status === 'pass'), 'JUnit ausente, falho ou pulado');
  assert.equal(browser.status, 0, 'Suíte Playwright falhou');
  assert.ok(!browserError && browserSpecs.length && browserSpecs.every(s => s.ok && s.tests.length && s.tests.every(t => t.status === 'expected' && t.expectedStatus === 'passed' && t.results.length && t.results.every(r => r.status === 'passed'))), 'Playwright ausente, falho ou pulado');
});
