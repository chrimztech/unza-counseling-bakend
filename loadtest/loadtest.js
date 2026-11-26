// Concurrent-user load test of the AI wellness chat (POST /ai-chat/message) against a LOCAL backend.
// Each virtual user is its own fake student account (loadtest.NN@counselling.test), logs in, then
// sends MSGS non-crisis messages one after another in its own conversation. Levels run in turn.
//   node loadtest/loadtest.js [levels=1,5,10,20] [msgs=5] [label]
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { BASE, login, api } = require('./common');

const LEVELS = (process.argv[2] || '1,5,10,20').split(',').map(Number);
const MSGS = Number(process.argv[3] || 5);
const LABEL = process.argv[4] || 'run';

// Everyday, non-crisis messages (none trigger the crisis detector).
const MESSAGES = [
  'I have exams next week and I cannot concentrate at all.',
  'My roommate and I keep arguing about noise in the room.',
  'I am worried about paying my tuition balance this semester.',
  'I feel homesick since I moved to Lusaka for school.',
  'How can I manage my time better between lectures and my part-time job?',
  'My parents expect me to do medicine but I want to study computing.',
  'I have not been sleeping well because of stress about assignments.',
  'I broke up with my boyfriend and I keep thinking about it.',
  'Where can I get help with my bursary application?',
  'I get nervous when I have to present in front of the class.',
];

const pct = (sorted, p) => sorted.length ? sorted[Math.min(sorted.length, Math.max(1, Math.ceil(p / 100 * sorted.length))) - 1] : null;

async function ensureUser(i, password) {
  const email = `loadtest.${String(i).padStart(2, '0')}@counselling.test`;
  await fetch(`${BASE}/auth/register`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, firstName: 'Load', lastName: `Test ${i}`, role: 'ROLE_STUDENT' }),
  }); // 400 "Email already in use" on reruns is fine
  return { email, password };
}

async function virtualUser(token, results) {
  const sessionId = 'lt_' + crypto.randomBytes(6).toString('hex');
  for (let m = 0; m < MSGS; m++) {
    const message = MESSAGES[Math.floor(Math.random() * MESSAGES.length)];
    const t0 = performance.now();
    let status = 0, aiAvailable = null, err = null;
    try {
      const r = await api(token, 'POST', '/ai-chat/message', { message, sessionId });
      status = r.status;
      aiAvailable = r.body?.data?.aiAvailable ?? null;
    } catch (e) { err = e.message; }
    results.push({ ms: performance.now() - t0, status, aiAvailable, err });
  }
}

(async () => {
  if (!/127\.0\.0\.1|localhost/.test(BASE)) throw new Error('Refusing to load-test a non-local API: ' + BASE);
  const pwFile = path.join(__dirname, '..', 'local-dev', 'loadtest-password.local.txt');
  if (!fs.existsSync(pwFile)) fs.writeFileSync(pwFile, crypto.randomBytes(12).toString('base64url') + '!a1');
  const password = fs.readFileSync(pwFile, 'utf8').trim();

  const maxUsers = Math.max(...LEVELS);
  const tokens = [];
  for (let i = 1; i <= maxUsers; i++) tokens.push(await login(await ensureUser(i, password)));

  const summary = [];
  for (const n of LEVELS) {
    const results = [];
    const t0 = performance.now();
    await Promise.all(tokens.slice(0, n).map(t => virtualUser(t, results)));
    const wall = (performance.now() - t0) / 1000;
    const ok = results.filter(r => r.status === 200 && r.aiAvailable !== false);
    const ms = ok.map(r => r.ms).sort((a, b) => a - b);
    const row = {
      users: n, requests: results.length, ok: ok.length,
      errors: results.length - ok.length,
      p50: Math.round(pct(ms, 50)), p95: Math.round(pct(ms, 95)), max: Math.round(ms[ms.length - 1] ?? 0),
      mean: Math.round(ms.reduce((a, b) => a + b, 0) / (ms.length || 1)),
      throughputPerMin: Math.round(results.length / wall * 60),
    };
    summary.push(row);
    console.log(JSON.stringify(row));
  }

  const outDir = path.join(__dirname, 'results');
  fs.mkdirSync(outDir, { recursive: true });
  const stamp = new Date().toISOString().replace(/[:.]/g, '-');
  fs.writeFileSync(path.join(outDir, `${stamp}-${LABEL}.json`), JSON.stringify({ label: LABEL, base: BASE, msgsPerUser: MSGS, at: new Date().toISOString(), summary }, null, 2));
})().catch(e => { console.error(e.message); process.exit(1); });
