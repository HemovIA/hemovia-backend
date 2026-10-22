import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import test from 'node:test';
import { readSnapshot, snapshotExists } from './helpers/snapshot.mjs';

const reportPath = '.spec/features/revisao-projeto/relatorios/api-dados-seguranca.md';
const report = readFileSync(reportPath, 'utf8');

test('@spec:AC-002 revisão de API, dados e segurança registra cobertura, evidências e achados reproduzíveis', () => {
  const areas = [
    'Arquitetura e camadas',
    'Contratos REST e validação',
    'Regras de doação, bolsas e estoque',
    'Dados, JPA e Flyway',
    'Autenticação e autorização',
    'Segredos e configuração',
    'Observabilidade de segurança',
  ];
  const requiredFindingFields = [
    '**Tipo:**',
    '**Severidade:**',
    '**Arquivo e linha:**',
    '**Esperado:**',
    '**Observado:**',
    '**Impacto:**',
    '**Reprodução ou justificativa estática:**',
    '**Recomendação:**',
  ];

  assert.ok(existsSync(reportPath), 'o relatório da revisão deve existir');
  for (const area of areas) assert.match(report, new RegExp(`\\| ${area} \\|`));

  const findings = report.match(/### API-\d{3}[\s\S]*?(?=\n### API-|\n## |$)/g) ?? [];
  assert.ok(findings.length >= 6, 'a revisão deve registrar achados ou riscos identificados');
  for (const finding of findings) {
    for (const field of requiredFindingFields) assert.ok(finding.includes(field), `${field} ausente em ${finding.match(/API-\d{3}/)[0]}`);
    assert.match(finding, /`api\/[^`]+:\d+(?:-\d+)?(?:,\d+(?:-\d+)?)*`/, 'cada achado deve apontar arquivo e linha');
  }

  for (const reference of [
    'api/src/main/java/com/hemovia/api/config/SecurityConfig.java',
    'api/src/main/java/com/hemovia/api/service/JwtService.java',
    'api/src/main/resources/db/migration/V2__create_users_table.sql',
    'api/src/main/resources/db/migration/V2__create_hospital_table.sql',
  ]) assert.ok(snapshotExists(reference), `referência inspecionada ausente no commit analisado: ${reference}`);

  for (const [, file, line] of report.matchAll(/`(api\/[^`:]+):(\d+)[^`]*`/g)) {
    assert.ok(snapshotExists(file), `referência inexistente no commit analisado: ${file}`);
    assert.ok(Number(line) <= readSnapshot(file).split(/\r?\n/).length,
      `linha fora do arquivo: ${file}:${line}`);
  }

  assert.match(report, /## Pontos positivos comprovados/);
  assert.match(report, /## Limitações e itens não verificados/);
  assert.match(report, /risco sem reprodução/);
  const jwtSource = readSnapshot('api/src/main/java/com/hemovia/api/service/JwtService.java');
  const embeddedFallback = jwtSource.match(/\$\{jwt\.secret:([^}]+)\}/)?.[1];
  assert.ok(embeddedFallback, 'o achado de fallback deve corresponder ao código analisado');
  assert.equal(report.includes(embeddedFallback), false, 'o relatório não deve divulgar o segredo JWT');
});
