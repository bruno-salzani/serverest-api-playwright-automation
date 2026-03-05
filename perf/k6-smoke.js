import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<800', 'p(99)<1500'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'https://serverest.dev';

export default function () {
  const resUsers = http.get(`${BASE_URL}/usuarios`);
  check(resUsers, { 'GET /usuarios 200': (r) => r.status === 200 });

  const login = http.post(`${BASE_URL}/login`, JSON.stringify({ email: 'naoexiste@teste.com', password: '1234' }), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(login, { 'POST /login 401': (r) => r.status === 401 });

  const products = http.get(`${BASE_URL}/produtos`);
  check(products, { 'GET /produtos 200': (r) => r.status === 200 });

  sleep(1);
}
