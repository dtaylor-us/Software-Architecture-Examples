/**
 * k6 load test: POST /price-updates (bulk) and GET /active-alerts.
 * Run: k6 run scripts/load-k6.js
 * Override: BASE_URL=http://localhost:8080 k6 run scripts/load-k6.js
 */
import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const postPayload = JSON.stringify([
  { nodeId: 'NODE-001', priceMwh: 100 },
  { nodeId: 'NODE-002', priceMwh: 100 },
  { nodeId: 'NODE-001', priceMwh: 250 },
  { nodeId: 'NODE-002', priceMwh: 90 },
]);

export const options = {
  scenarios: {
    post_updates: {
      executor: 'constant-vus',
      vus: 20,
      duration: '30s',
      startTime: '0s',
      exec: 'postUpdates',
    },
    get_alerts: {
      executor: 'constant-vus',
      vus: 10,
      duration: '30s',
      startTime: '0s',
      exec: 'getAlerts',
    },
  },
};

export function postUpdates() {
  const res = http.post(
    `${BASE_URL}/price-updates`,
    postPayload,
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { 'POST /price-updates status 200': (r) => r.status === 200 });
}

export function getAlerts() {
  const res = http.get(`${BASE_URL}/active-alerts`);
  check(res, { 'GET /active-alerts status 200': (r) => r.status === 200 });
}
