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
  login: (login, password) =>
    request("/api/auth/login", { method: "POST", body: { login, password } }),
  register: (login, password, fullName, email) =>
    request("/api/auth/register", {
      method: "POST",
      body: { login, password, fullName, email },
    }),
  searchEvents: (keyword = "", page = 0, pageSize = 20) => {
    const q = new URLSearchParams({ page, pageSize });
    if (keyword) q.set("keyword", keyword);
    return request(`/api/events/search?${q}`);
  },
  getEvent: (id) => request(`/api/events/${id}`),
  reserve: (ticketIds) =>
    request("/api/bookings/reserve", { method: "POST", body: { ticketIds }, auth: true }),
  confirm: (bookingId, card) =>
    request("/api/bookings/confirm", {
      method: "POST",
      body: { bookingId, ...card },
      auth: true,
    }),
  createVenue: (venue) =>
    request("/api/admin/venues", { method: "POST", body: venue, auth: true }),
  createPerformer: (performer) =>
    request("/api/admin/performers", { method: "POST", body: performer, auth: true }),
  createEvent: (event) =>
    request("/api/admin/events", { method: "POST", body: event, auth: true }),
  createSchedule: (run) =>
    request("/api/admin/events/schedule", { method: "POST", body: run, auth: true }),
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
