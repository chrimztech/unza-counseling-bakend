// OpenAI-compatible stub for local load tests: POST /v1/chat/completions (or /chat/completions).
// Replies after a fixed delay (+ jitter) so the backend's own overhead under load can be measured
// separately from a real provider's latency. No API key needed; never use in production.
//   node loadtest/mock-llm.js [port=9099] [delayMs=1500] [jitterMs=300]
const http = require('http');
const port = Number(process.argv[2] || 9099);
const delay = Number(process.argv[3] || 1500);
const jitter = Number(process.argv[4] || 300);
let served = 0;

http.createServer((req, res) => {
  if (req.method === 'GET' && req.url.endsWith('/models')) {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({ data: [{ id: 'mock-model' }] }));
  }
  if (req.method !== 'POST' || !req.url.endsWith('/chat/completions')) {
    res.writeHead(404); return res.end();
  }
  let body = '';
  req.on('data', c => (body += c));
  req.on('end', () => {
    const wait = delay + Math.round((Math.random() * 2 - 1) * jitter);
    setTimeout(() => {
      served++;
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        id: 'mock-' + served, object: 'chat.completion', model: 'mock-model',
        choices: [{ index: 0, finish_reason: 'stop', message: { role: 'assistant',
          content: 'Thank you for sharing that with me. It sounds like a lot to carry. (mock reply for load testing)' } }],
        usage: { prompt_tokens: Math.round(body.length / 4), completion_tokens: 20, total_tokens: Math.round(body.length / 4) + 20 },
      }));
    }, Math.max(wait, 0));
  });
}).listen(port, '127.0.0.1', () => console.log(`mock LLM on http://127.0.0.1:${port} delay=${delay}±${jitter}ms`));
