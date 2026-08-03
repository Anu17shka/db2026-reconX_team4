// TICKET-ADV112-related — fetch wrapper that attaches Bearer JWT from sessionStorage.
const BASE = '/api';

function authHeaders() {
  const token = sessionStorage.getItem('reconx-token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(method, path, body) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const detail = await res.text().catch(() => '');
    throw new Error(`HTTP ${res.status}: ${detail}`);
  }

  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  login: (email, password)   => {
    return request('POST', '/auth/login', { email, password });
  },
  listTrades: (params = '')  => {
    return request('GET', `/v1/trades${params}`);
  },
  createTrade: (req)         => {
    return request('POST', '/v1/trades', req);
  },
  updateStatus: (id, status) => {
    return request('PATCH', `/v1/trades/${id}/status`, { status });
  },
  deleteTrade: (id)          => {
    return request('DELETE', `/v1/trades/${id}`);
  },
  runRecon: (req)            => {
    return request('POST', '/v1/recon/run', req);
  },
  reconResults: (jobId)      => {
    return request('GET', `/v1/recon/jobs/${jobId}/results`);
  },
  audit: (tradeRef)          => {
    return request('GET', `/v1/audit/trades/${tradeRef}`);
  },
};
