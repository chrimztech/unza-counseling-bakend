// Sends a handful of crisis-language messages from fake LOCAL students so the AI Escalations
// queue has realistic content for screenshots. One message per student (alerts de-duplicate per
// student). Includes a likely false positive for the counsellor to mark. Local only.
const fs = require('fs');
const path = require('path');
const { BASE, accounts, login, api } = require('./common');

const CASES = [
  { who: 'student', text: 'Honestly I feel like everyone would be better off without me. I failed two courses and my family paid so much.' },
  { who: 'loadtest.01', text: 'I feel hopeless and I want to kill myself, I cannot face my parents after these results.' },
  { who: 'loadtest.02', text: 'My boyfriend is hitting me when he gets drunk and I am scared to go back to the room.' },
  { who: 'loadtest.03', text: 'I can\'t go on like this anymore, I am so tired of everything at school.' },
  { who: 'loadtest.04', text: 'For my psychology assignment I am reading about suicide prevention among university students. Any resources?' },
];

(async () => {
  if (!/127\.0\.0\.1|localhost/.test(BASE)) throw new Error('Refusing to run against a non-local API: ' + BASE);
  const acc = accounts();
  const ltPw = fs.readFileSync(path.join(__dirname, '..', 'local-dev', 'loadtest-password.local.txt'), 'utf8').trim();
  for (const c of CASES) {
    const cred = c.who === 'student' ? acc.student : { email: `${c.who}@counselling.test`, password: ltPw };
    const token = await login(cred);
    const r = await api(token, 'POST', '/ai-chat/message', { message: c.text, sessionId: 'seed_escalation' });
    console.log(cred.email, r.status, 'crisisDetected=' + r.body?.data?.crisisDetected);
  }
})().catch(e => { console.error(e.message); process.exit(1); });
