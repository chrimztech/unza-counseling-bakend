// One-off local setup: imports the counselling knowledge workbook and registers the mock LLM
// as the default AI provider. Run against a LOCAL backend only.
//   node loadtest/setup-local.js <path-to-workbook.xlsx> [mockUrl=http://127.0.0.1:9099/v1]
const fs = require('fs');
const path = require('path');
const { accounts, login, api, BASE } = require('./common');

(async () => {
  if (!/127\.0\.0\.1|localhost/.test(BASE)) throw new Error('Refusing to run against a non-local API: ' + BASE);
  const workbook = process.argv[2];
  const mockUrl = process.argv[3] || 'http://127.0.0.1:9099/v1';
  const token = await login(accounts().admin);

  if (workbook) {
    const fd = new FormData();
    fd.append('file', new Blob([fs.readFileSync(workbook)]), path.basename(workbook));
    const r = await api(token, 'POST', '/admin/ai-knowledge/import', fd);
    console.log('knowledge import:', r.status, JSON.stringify(r.body).slice(0, 300));
  }

  const existing = await api(token, 'GET', '/admin/ai-providers');
  const list = Array.isArray(existing.body) ? existing.body : existing.body?.data || [];
  if (!list.some(p => p.name === 'Local mock LLM')) {
    const r = await api(token, 'POST', '/admin/ai-providers', {
      name: 'Local mock LLM', providerType: 'CUSTOM', baseUrl: mockUrl, model: 'mock-model',
      enabled: true, isDefault: true, priority: 1, maxTokens: 512, timeoutSeconds: 30,
    });
    console.log('provider:', r.status, JSON.stringify(r.body).slice(0, 200));
  } else {
    console.log('provider: already present');
  }
  const stats = await api(token, 'GET', '/admin/ai-knowledge/stats');
  console.log('knowledge stats:', JSON.stringify(stats.body).slice(0, 300));
})().catch(e => { console.error(e.message); process.exit(1); });
