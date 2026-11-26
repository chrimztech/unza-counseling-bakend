// Shared helpers for the local load-test scripts. Reads the fake local accounts from
// local-dev/test-accounts.local.txt (git-ignored). Local development only.
const fs = require('fs');
const path = require('path');

const BASE = process.env.API_BASE || 'http://127.0.0.1:8080/api';

function accounts() {
  const file = path.join(__dirname, '..', 'local-dev', 'test-accounts.local.txt');
  const out = {};
  for (const line of fs.readFileSync(file, 'utf8').split(/\r?\n/)) {
    const m = line.match(/^(\S+@counselling\.test)\s+(\S+)/);
    if (m) out[m[1].split('.')[1].split('@')[0]] = { email: m[1], password: m[2] };
  }
  return out; // { student, counsellor, admin }
}

async function login({ email, password }) {
  const res = await fetch(`${BASE}/auth/login`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ identifier: email, password }),
  });
  const body = await res.json();
  const token = body.token || body.accessToken || body.data?.token || body.data?.accessToken;
  if (!token) throw new Error(`login failed for ${email}: ${res.status} ${JSON.stringify(body).slice(0, 200)}`);
  return token;
}

async function api(token, method, url, body) {
  const res = await fetch(`${BASE}${url}`, {
    method,
    headers: { Authorization: `Bearer ${token}`, ...(body && !(body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}) },
    body: body instanceof FormData ? body : body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let json; try { json = JSON.parse(text); } catch { json = text; }
  return { status: res.status, body: json };
}

module.exports = { BASE, accounts, login, api };
