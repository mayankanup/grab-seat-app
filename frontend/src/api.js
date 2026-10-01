// Same-origin by default: Vite dev proxy and Docker nginx both forward
// /auth, /events, /bookings and /api to the backend. Set VITE_API_URL
// only to point the SPA at a remote backend directly (needs CORS).
const API_BASE = import.meta.env.VITE_API_URL ?? "";

function authHeaders() {
  const token = localStorage.getItem("grabseat_token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(path, { method = "GET", body, auth = false } = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(auth ? authHeaders() : {}),
    },
    ...(body ? { body: JSON.stringify(body) } : {}),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.error ?? `Request failed (${res.status})`);
  }
  return data;
}

export const api = {
  login: (userId) =>
    request("/auth/login", { method: "POST", body: { userId } }),
  searchEvents: (keyword = "", page = 0, pageSize = 20) => {
    const q = new URLSearchParams({ page, pageSize });
    if (keyword) q.set("keyword", keyword);
    return request(`/events/search?${q}`);
  },
  getEvent: (id) => request(`/events/${id}`),
  reserve: (ticketIds) =>
    request("/bookings/reserve", { method: "POST", body: { ticketIds }, auth: true }),
  confirm: (bookingId, card) =>
    request("/bookings/confirm", {
      method: "POST",
      body: { bookingId, ...card },
      auth: true,
    }),
};

export function tokenPayload() {
  const token = localStorage.getItem("grabseat_token");
  if (!token) return null;
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch {
    return null;
  }
}

export function isTokenValid() {
  const payload = tokenPayload();
  return !!payload?.sub && (!payload.exp || payload.exp * 1000 > Date.now());
}
