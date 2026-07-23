import http from 'k6/http';
import { check, sleep } from 'k6';

// k6 configuration for 100 concurrent virtual users for 1 minute
export const options = {
  vus: 100,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    http_req_failed: ['rate<0.01'],    // error rate must be below 1%
  },
};

export default function () {
  const url = __ENV.BASE_URL || 'http://localhost:8001';
  
  // 1. Load Homepage Storefront
  const res1 = http.get(`${url}/`);
  check(res1, {
    'status is 200': (r) => r.status === 200,
    'body contains GroceryConnect': (r) => r.body.includes('GroceryConnect') || r.body.includes('shop'),
  });
  sleep(1);

  // 2. Query shop profile endpoint
  const res2 = http.get(`${url}/get_shop_details.php?shop_id=1`);
  check(res2, {
    'shop detail status is 200': (r) => r.status === 200,
  });
  sleep(1);
}
