import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const directory = path.dirname(fileURLToPath(import.meta.url));
const reportPath = path.resolve(directory, '../../.spec/features/revisao-projeto/relatorios/alinhamento.md');
const documentedStories = [
  ...Array.from({ length: 12 }, (_, index) => `POOUS${String(index + 1).padStart(2, '0')}`),
  ...Array.from({ length: 10 }, (_, index) => `AEDUS${String(index + 1).padStart(2, '0')}`),
  ...Array.from({ length: 7 }, (_, index) => `ESTUS${String(index + 1).padStart(2, '0')}`),
  ...Array.from({ length: 7 }, (_, index) => `SOUS${String(index + 1).padStart(2, '0')}`),
  ...Array.from({ length: 8 }, (_, index) => `RSDUS${String(index + 1).padStart(2, '0')}`),
];

test('@spec:AC-001 — matriz inventaria componentes e todas as funcionalidades documentadas', () => {
  const report = readFileSync(reportPath, 'utf8');

  const root = path.resolve(directory, '../..');
  const sourceStories = [...readFileSync(path.join(root, 'diagramas/README.md'), 'utf8')
    .matchAll(/^### ((?:POO|AED|EST|SO|RSD)US\d{2})\b/gm)].map(match => match[1]);
  assert.deepEqual(new Set(documentedStories), new Set(sourceStories), 'a matriz deve cobrir o backlog real');
  for (const [, source, range] of report.matchAll(/`([^`\s]+\.(?:java|c|sql|md|xml|yaml|yml)):(\d+(?:-\d+)?(?:,\d+(?:-\d+)?)*)`/g)) {
    const length = readFileSync(path.join(root, source), 'utf8').split(/\r?\n/).length;
    for (const line of range.split(/[,-]/).map(Number)) {
      assert.ok(line > 0 && line <= length, `referência fora do arquivo: ${source}:${line}`);
    }
  }

  for (const story of documentedStories) {
    assert.match(report, new RegExp(`\\| ${story} — .+? \\| (implementada|parcial|planejada|sem evidência) \\|`));
  }

  assert.match(report, /## Componentes encontrados/);
  assert.match(report, /## Matriz das funcionalidades de README e diagramas/);
  assert.match(report, /api\/src\/main\/java\/com\/hemovia\/api\/ApiApplication\.java:6/);
  assert.match(report, /BloodBagServiceImplTest\.java:36-57/);
  assert.match(report, /não é\s+achado de defeito/);
  assert.match(report, /não aprova comportamento funcional Java\/C/);
});

// Esta prova valida somente a completude do artefato AC-001; não é aprovação funcional Java/C.
