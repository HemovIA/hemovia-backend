import { readFileSync, readdirSync, statSync } from 'node:fs';
import path from 'node:path';

const decode = value => value.replace(/&(?:amp|lt|gt|quot|apos|#\d+|#x[0-9a-f]+);/gi, token => {
  const named = { '&amp;': '&', '&lt;': '<', '&gt;': '>', '&quot;': '"', '&apos;': "'" };
  if (named[token]) return named[token];
  return String.fromCodePoint(token.startsWith('&#x') ? parseInt(token.slice(3, -1), 16) : Number(token.slice(2, -1)));
});
const attributes = source => Object.fromEntries([...source.matchAll(/([\w:.-]+)\s*=\s*(["'])(.*?)\2/g)].map(m => [m[1], decode(m[3])]));

// Leitor restrito ao formato Surefire local; não resolve DTDs nem entidades externas.
export function parseSurefire(xml) {
  if (/<!DOCTYPE|<!ENTITY/i.test(xml)) throw new Error('DTD/entidade externa não é relatório Surefire aceito');
  const suite = xml.match(/<testsuite\b([^>]*)>/);
  if (!suite || !xml.includes('</testsuite>')) throw new Error('Relatório Surefire incompleto');
  const header = attributes(suite[1]);
  const cases = [...xml.matchAll(/<testcase\b([^>]*?)(?:\/>|>([\s\S]*?)<\/testcase\s*>)/g)].map(m => {
    const attrs = attributes(m[1]);
    if (!attrs.name || !attrs.classname) throw new Error('Caso sem classe/nome');
    const body = m[2] ?? '';
    const failure = /<failure\b/.test(body);
    const error = /<error\b/.test(body);
    const status = failure || error ? 'fail' : /<skipped\b/.test(body) ? 'skip' : 'pass';
    return { className: attrs.classname, method: attrs.name.replace(/(?:\(|\[).*$/, ''), name: attrs.name, status, failure, error };
  });
  if (!Number.isInteger(Number(header.tests)) || Number(header.tests) !== cases.length) throw new Error('Contagem de casos divergente/incompleta');
  for (const field of ['failures', 'errors', 'skipped']) {
    if (header[field] !== undefined && (!Number.isInteger(Number(header[field])) || Number(header[field]) < 0))
      throw new Error(`Invalid ${field} count`);
  }
  const observed = {
    failures: cases.filter(test => test.failure).length,
    errors: cases.filter(test => test.error).length,
    skipped: cases.filter(test => test.status === 'skip').length,
  };
  for (const field of ['failures', 'errors', 'skipped']) {
    if (header[field] !== undefined && Number(header[field]) !== observed[field])
      throw new Error(`Divergent/incomplete ${field} count`);
  }
  return cases;
}

export function readFreshReports(directory, startedAt) {
  const files = readdirSync(directory).filter(f => /^TEST-.*\.xml$/.test(f));
  if (!files.length) throw new Error('Nenhum relatório Surefire nesta execução');
  return files.flatMap(file => {
    const full = path.join(directory, file);
    if (statSync(full).mtimeMs < startedAt) throw new Error(`Relatório antigo: ${file}`);
    return parseSurefire(readFileSync(full, 'utf8'));
  });
}

export function assessCriterion(cases, required) {
  if (!required?.length) return { pass: false, reason: 'Critério sem casos obrigatórios' };
  for (const key of required) {
    const found = cases.filter(c => `${c.className}#${c.method}` === key);
    if (!found.length) return { pass: false, reason: `Caso obrigatório não executado: ${key}` };
    if (found.some(c => c.status !== 'pass')) return { pass: false, reason: `Falha/skip em ${key}` };
  }
  return { pass: true };
}
