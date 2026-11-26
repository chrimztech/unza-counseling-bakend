# AI chat load test

Scripts to measure how the AI wellness chat (`POST /api/ai-chat/message`) behaves with many students at once.
They only run against a **local** backend (they refuse any other host).

| Script | Purpose |
|---|---|
| `mock-llm.js` | OpenAI-compatible stub model with a fixed delay, so the system's own cost can be separated from a real provider's latency, without spending provider credit. |
| `setup-local.js` | Imports the knowledge workbook and registers the stub as the default provider. |
| `loadtest.js` | N concurrent fake students (`loadtest.NN@counselling.test`), each sending 5 everyday (non-crisis) messages in its own conversation. |
| `seed-escalations.js` | A few crisis-language messages from fake students, for the AI Escalations screen. |

Test accounts and passwords live in `local-dev/` (git-ignored).

## Results — 26 Nov 2026

**Environment.** Single laptop running everything: Intel Core i7-10750H (6 cores / 12 threads), 24 GB RAM, Windows 11.
- Software: Java 21, Spring Boot 3.1.5, PostgreSQL 18.
- Knowledge base: 676 counsellor scenarios, the same set as production.
- Timing: end-to-end at the client (login excluded). Each request includes:
  - the crisis scan;
  - memory and records lookup;
  - BM25 scenario retrieval;
  - the model call;
  - saving the exchange.

### A. Model replying in 1.5 s ± 0.3 s (close to DeepSeek's measured single-user time)

| Concurrent students | Requests | Errors | Median | p95 | Max |
|---:|---:|---:|---:|---:|---:|
| 1  | 5   | 0 | 1.81 s | 1.94 s | 1.94 s |
| 5  | 25  | 0 | 1.62 s | 1.88 s | 1.90 s |
| 10 | 50  | 0 | 1.59 s | 1.89 s | 1.98 s |
| 20 | 100 | 0 | 1.67 s | 1.90 s | 2.00 s |
| 50 | 250 | 0 | 1.68 s | 2.42 s | 2.82 s |

### B. Model replying instantly (the system's own overhead)

| Concurrent students | Requests | Errors | Median | p95 | Max |
|---:|---:|---:|---:|---:|---:|
| 1  | 5   | 0 | 44 ms  | 52 ms  | 52 ms |
| 5  | 25  | 0 | 42 ms  | 51 ms  | 51 ms |
| 10 | 50  | 0 | 80 ms  | 114 ms | 545 ms |
| 20 | 100 | 0 | 160 ms | 272 ms | 406 ms |
| 50 | 250 | 0 | 477 ms | 844 ms | 1.11 s |

The system adds about 0.05 s per message for one user. At 50 students sending at the same moment, it adds about 0.5 s (median). It sustained roughly 5,500 messages per minute with no errors. In practice the AI provider's response time dominates, and the provider's own rate limits are the likelier ceiling.

**Limits.**
- These are local-machine figures with a stub model, not production measurements.
- The production server has different hardware.
- A real provider adds network latency and its own variability.
- Production single-user timings measured earlier with DeepSeek: 1.8–2.0 s average, 3.0 s maximum, over 24 messages.

Raw output is in `results/`.
