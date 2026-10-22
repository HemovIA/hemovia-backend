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
