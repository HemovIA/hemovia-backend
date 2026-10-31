import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync, utimesSync, unlinkSync, rmdirSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';
import test from 'node:test';
import { assessCriterion, parseSurefire, readFreshReports } from '../../scripts/spec-java/read-surefire.mjs';

const report = body => `<testsuite tests="1">${body}</testsuite>`;
const testcase = body => `<testcase classname="example.DomainTest" name="ac005_case">${body}</testcase>`;
const required = ['example.DomainTest#ac005_case'];

test('@spec:AC-014 um caso realmente aprovado pode produzir prova', () => {
  assert.equal(assessCriterion(parseSurefire(report(testcase(''))), required).pass, true);
});
test('@spec:AC-014 failure, error e skip nunca produzem PASS', () => {
  for (const body of ['<failure message="x"/>', '<error message="x"/>', '<skipped/>'])
    assert.equal(assessCriterion(parseSurefire(report(testcase(body))), required).pass, false);
});
test('@spec:AC-014 ausência de um caso não é compensada por outro que passou', () => {
  assert.equal(assessCriterion(parseSurefire(report(testcase(''))), [...required, 'example.DomainTest#missing']).pass, false);
  assert.equal(assessCriterion([], []).pass, false);
});
test('@spec:AC-014 falha em uma parametrização domina outra que passou', () => {
  const cases = [{ className: 'example.DomainTest', method: 'ac005_case', status: 'pass' },
    { className: 'example.DomainTest', method: 'ac005_case', status: 'fail' }];
  assert.equal(assessCriterion(cases, required).pass, false);
});
test('@spec:AC-014 XML truncado ou com contagem divergente é recusado', () => {
  assert.throws(() => parseSurefire('<testsuite tests="1">'), /incompleto/);
  assert.throws(() => parseSurefire('<testsuite tests="1"></testsuite>'), /Contagem/);
  assert.throws(() => parseSurefire('<!DOCTYPE x><testsuite tests="0"></testsuite>'), /DTD/);
});
test('@spec:AC-014 contagens de falha, erro e skip também precisam corresponder aos casos', () => {
  assert.throws(() => parseSurefire('<testsuite tests="1" failures="1"><testcase classname="example.DomainTest" name="ac005_case"/></testsuite>'), /count/i);
  assert.throws(() => parseSurefire('<testsuite tests="1" errors="1"><testcase classname="example.DomainTest" name="ac005_case"><failure/></testcase></testsuite>'), /count/i);
});
test('@spec:AC-014 relatório antigo e diretório sem relatórios são recusados', () => {
  const directory = mkdtempSync(path.join(tmpdir(), 'hemovia-surefire-'));
  const file = path.join(directory, 'TEST-example.xml');
  try {
    assert.throws(() => readFreshReports(directory, Date.now()), /Nenhum/);
    writeFileSync(file, report(testcase(''))); utimesSync(file, new Date(0), new Date(0));
    assert.throws(() => readFreshReports(directory, Date.now()), /antigo/);
  } finally { unlinkSync(file); rmdirSync(directory); }
});
