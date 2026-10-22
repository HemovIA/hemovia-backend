import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const reportPath = new URL('../../.spec/features/revisao-projeto/relatorios/qualidade-operacao.md', import.meta.url);

test('@spec:AC-003 revisa AED, testes e operação com evidências, comandos e limitações', async () => {
  const report = await readFile(reportPath, 'utf8');

  for (const [, file, line] of report.matchAll(/`((?:api|aed-u1)\/[^`:]+):(\d+)[^`]*`/g)) {
    const source = await readFile(new URL(`../../${file}`, import.meta.url), 'utf8');
    assert.ok(Number(line) <= source.split(/\r?\n/).length, `linha fora do arquivo: ${file}:${line}`);
  }

  for (const section of [
    '## Estruturas de dados (AED)',
    '## Testes e build',
    '## Banco, Docker, automação e documentação',
    '## Comandos executados e limitações',
    '## Achados priorizados',
  ]) {
    assert.match(report, new RegExp(section.replace(/[()]/g, '\\$&')));
  }

  for (const invariantOrEdgeCase of [
    'Lista de estoque C',
    'Fila FIFO C e Java',
    'Pilha/undo C e Java',
    'ABB e recursão',
    'fila vazia',
    'entrada ordenada/degenerada',
  ]) {
    assert.match(report, new RegExp(invariantOrEdgeCase));
  }

  for (const command of ['`java -version`', '`mvn -v`', '`gcc --version`', '`docker`']) {
    assert.match(report, new RegExp(command.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
  }

  assert.match(report, /\| Comando \| Diretório \| Resultado \| Limitação \/ interpretação \|/);
  assert.match(report, /Não verificado/);
  assert.match(report, /não é aprovação de execução/);

  for (const finding of ['QUA-001', 'QUA-002', 'QUA-003']) {
    assert.match(report, new RegExp(finding));
  }
  for (const requiredFindingField of [
    'Tipo / severidade',
    'Arquivo e linhas',
    'Esperado',
    'Observado',
    'Impacto',
    'Reprodução ou justificativa estática',
    'Recomendação',
  ]) {
    assert.equal(report.split(requiredFindingField).length - 1 >= 3, true,
      `cada achado precisa registrar ${requiredFindingField}`);
  }
});
