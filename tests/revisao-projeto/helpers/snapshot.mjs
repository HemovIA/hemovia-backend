import { spawnSync } from 'node:child_process';

export const reviewedCommit = '91d1f981e8860df0cb53fa9472edd8f1bcd91e89';

function revisionPath(relative) {
  if (!relative || relative.startsWith('/') || relative.includes('..')) throw new Error(`Caminho histórico inválido: ${relative}`);
  return `${reviewedCommit}:${relative.replaceAll('\\', '/')}`;
}

export function readSnapshot(relative) {
  const result = spawnSync('git', ['show', revisionPath(relative)], { encoding: 'utf8' });
  if (result.error) throw new Error(`Não foi possível consultar o commit histórico: ${result.error.message}`);
  if (result.status !== 0) throw new Error(`Evidência ausente no commit ${reviewedCommit}: ${relative}`);
  return result.stdout;
}

export function snapshotExists(relative) {
  return spawnSync('git', ['cat-file', '-e', revisionPath(relative)], { encoding: 'utf8' }).status === 0;
}
