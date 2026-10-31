import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import path from 'node:path';
import test from 'node:test';

const root = path.resolve(import.meta.dirname, '../..');
const feature = path.join(root, '.spec/features/revisao-projeto');
const reports = [
  'relatorios/alinhamento.md',
  'relatorios/api-dados-seguranca.md',
  'relatorios/qualidade-operacao.md',
];
const finalPath = path.join(feature, 'relatorio-final.md');
const read = relative => readFileSync(path.join(feature, relative), 'utf8');
const ids = text => [...text.matchAll(/\b(?:API|QUA)-\d{3}\b/g)].map(match => match[0]);

test('@spec:AC-004 consolida achados dos relatórios em plano rastreável', () => {
  const final = readFileSync(finalPath, 'utf8');
  const reportIds = new Set(reports.flatMap(report => ids(read(report))));
  const storyRows = read('relatorios/alinhamento.md').split('\n')
    .filter(line => /^\| (POO|AED|EST|SO|RSD)US\d{2}/.test(line));
  const counts = {};
  for (const row of storyRows) {
    const status = row.split('|')[2].trim();
    counts[status] = (counts[status] ?? 0) + 1;
  }
  assert.match(final, new RegExp(`${storyRows.length} histórias: ${counts.implementada} implementadas estaticamente, ${counts.parcial} parciais,\\s+${counts.planejada} planejadas e ${counts['sem evidência']} sem evidência`));
  const plan = final.match(/## Plano de ação rastreável([\s\S]*?)(?=\n## |$)/)?.[1];
  assert.ok(plan, 'o relatório final deve conter plano de ação');

  for (const id of reportIds) {
    const row = plan.split('\n').find(line => line.includes(`| ${id}`) || line.includes(`, ${id}`));
    assert.ok(row, `${id} deve estar coberto no plano (aliases podem compartilhar uma linha)`);
    assert.equal(row.split('|').length, 7, `${id} deve possuir as cinco colunas do plano`);
    for (const field of row.split('|').slice(2, 6)) {
      assert.notEqual(field.trim(), '', `${id} deve ter prioridade, justificativa, dependências e validação`);
    }
  }

  assert.match(plan, /API-001, QUA-001/, 'Flyway duplicado deve ser deduplicado preservando aliases');
  assert.match(final, /91d1f981e8860df0cb53fa9472edd8f1bcd91e89/);
  assert.match(final, /33c3420/);
  assert.match(final, /16 passaram, 1 errou[\s\S]*suíte \*\*FAIL\*\*, 0 skip/);
  assert.match(final, /não prova registro de parâmetros pessoais/i);
  assert.match(final, /não prova que endpoints sensíveis estejam expostos/i);
  assert.match(final, /testes Node[\s\S]*?não provam[\s\S]*?comportamento Java\/C/i);

  const localLinks = [...final.matchAll(/\]\(([^)]+\.md)\)/g)].map(match => match[1]);
  assert.ok(localLinks.length >= 4, 'o relatório deve ligar suas fontes locais');
  for (const link of localLinks) {
    assert.ok(existsSync(path.resolve(feature, link)), `link local existente: ${link}`);
  }
});
