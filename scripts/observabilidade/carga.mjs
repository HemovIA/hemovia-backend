import { randomInt, randomUUID } from 'node:crypto';
import { setTimeout as delay } from 'node:timers/promises';

const base = process.env.HEMOVIA_API_URL ?? 'http://127.0.0.1:18080/api/v1';
if (!['127.0.0.1', 'localhost', '[::1]'].includes(new URL(base).hostname)) throw new Error('A carga aceita somente API local');
if (!process.env.ADMIN_EMAIL || !process.env.ADMIN_PASSWORD) throw new Error('ADMIN_EMAIL e ADMIN_PASSWORD são necessários');

class Session {
  cookies = new Map();
  async request(path, body, expected = 200) {
    if (body !== undefined && path !== '/auth/login') await this.request('/auth/csrf', undefined, 204);
    const headers = {};
    if (this.cookies.size) headers.cookie = [...this.cookies].map(([k, v]) => `${k}=${v}`).join('; ');
    if (body !== undefined) {
      headers['content-type'] = 'application/json';
      if (this.cookies.has('XSRF-TOKEN')) headers['x-xsrf-token'] = decodeURIComponent(this.cookies.get('XSRF-TOKEN'));
    }
    const response = await fetch(base + path, { method: body === undefined ? 'GET' : 'POST', headers, body: body === undefined ? undefined : JSON.stringify(body) });
    for (const cookie of response.headers.getSetCookie()) {
      const [pair] = cookie.split(';'); const i = pair.indexOf('=');
      this.cookies.set(pair.slice(0, i), pair.slice(i + 1));
    }
    if (response.status !== expected) throw new Error(`${path}: HTTP ${response.status}, esperado ${expected}`);
    return response.status === 204 ? null : response.json();
  }
  async login(identifier, password) {
    await this.request('/auth/csrf', undefined, 204);
    await this.request('/auth/login', { identifier, password });
    await this.request('/auth/csrf', undefined, 204);
  }
}

function cpf() {
  const digits = Array.from({ length: 9 }, () => randomInt(10));
  for (let length = 9; length <= 10; length++) {
    const sum = digits.reduce((n, d, i) => n + d * (length + 1 - i), 0);
    const remainder = (sum * 10) % 11;
    digits.push(remainder === 10 ? 0 : remainder);
  }
  return digits.join('');
}

const admin = new Session();
await admin.login(process.env.ADMIN_EMAIL, process.env.ADMIN_PASSWORD);
const suffix = randomUUID();
const hospital = await admin.request('/hospitals', { name: `Observabilidade ${suffix}`, city: 'São Paulo', latitude: -23.55, longitude: -46.63, priority: 3 }, 201);
const email = `observabilidade-${suffix}@example.test`;
const password = randomUUID();
await admin.request('/users', { name: 'Observabilidade', email, cpf: cpf(), password, role: 'HOSPITAL', hospitalId: hospital.id }, 201);
const user = new Session();
await user.login(email, password);

async function cycle() {
  const today = new Date();
  await admin.request('/donations', { collectionCenter: 'Observabilidade', bloodType: 'O', rhFactor: 'POSITIVE', component: 'WHOLE_BLOOD', collectionDate: today.toISOString().slice(0, 10), expirationDate: new Date(today.getTime() + 30 * 86400000).toISOString().slice(0, 10) }, 201);
  const request = { bloodType: 'O', rhFactor: 'POSITIVE', component: 'RED_BLOOD_CELLS', quantity: 1, priority: 'MEDIUM', deliveryDeadline: new Date(today.getTime() + 86400000).toISOString() };
  await user.request('/requests', request, 201);
  await user.request('/requests', { ...request, deliveryDeadline: new Date(today.getTime() - 86400000).toISOString() }, 400);
  console.log('Carga sintética concluída: doação, requisição e erro esperado.');
}
let lastLogin = Date.now();
do {
  if (Date.now() - lastLogin > 10 * 60_000) {
    await admin.login(process.env.ADMIN_EMAIL, process.env.ADMIN_PASSWORD);
    await user.login(email, password);
    lastLogin = Date.now();
  }
  await cycle();
  if (process.argv.includes('--loop')) await delay(15000);
} while (process.argv.includes('--loop'));
