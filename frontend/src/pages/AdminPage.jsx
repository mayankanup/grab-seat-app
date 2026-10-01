import { useState } from "react";
import { api } from "../api";

function Section({ title, children }) {
  return (
    <div className="card">
      <h3>{title}</h3>
      {children}
    </div>
  );
}

function Result({ result, error }) {
  if (error) return <p className="error">{error}</p>;
  if (!result) return null;
  return <pre className="muted">{JSON.stringify(result, null, 2)}</pre>;
}

export default function AdminPage() {
  const [venue, setVenue] = useState({ name: "", location: "", capacity: 100 });
  const [performer, setPerformer] = useState({ name: "", type: "COMEDIAN" });
  const [event, setEvent] = useState({
    name: "",
    description: "",
    type: "COMEDY",
    venueId: "",
    performerId: "",
    startTime: "",
    endTime: "",
    basePrice: "",
    ticketCount: 40,
  });
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [run, setRun] = useState({
    name: "",
    description: "",
    type: "MOVIE",
    venueId: "",
    performerId: "",
    basePrice: "",
    ticketCount: 40,
    startDate: "",
    endDate: "",
    showTimes: "08:00, 10:00",
    durationMinutes: 90,
  });

  async function submit(fn, payload, coerce) {
    setError("");
    setResult(null);
    try {
      setResult(await fn(coerce ? coerce(payload) : payload));
    } catch (err) {
      setError(err.message);
    }
  }

  const num = (v) => (v === "" ? null : Number(v));

  return (
    <div>
      <h2>Admin</h2>
      <Section title="Create venue">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            submit(api.createVenue, venue, (v) => ({ ...v, capacity: Number(v.capacity) }));
          }}
        >
          <input placeholder="name" value={venue.name} onChange={(e) => setVenue({ ...venue, name: e.target.value })} />
          <input placeholder="location" value={venue.location} onChange={(e) => setVenue({ ...venue, location: e.target.value })} />
          <input placeholder="capacity" value={venue.capacity} onChange={(e) => setVenue({ ...venue, capacity: e.target.value })} />
          <button type="submit">Create venue</button>
        </form>
      </Section>
      <Section title="Create performer">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            submit(api.createPerformer, performer);
          }}
        >
          <input placeholder="name" value={performer.name} onChange={(e) => setPerformer({ ...performer, name: e.target.value })} />
          <input placeholder="type, e.g. COMEDIAN" value={performer.type} onChange={(e) => setPerformer({ ...performer, type: e.target.value })} />
          <button type="submit">Create performer</button>
        </form>
      </Section>
      <Section title="Create event">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            submit(api.createEvent, event, (v) => ({
              ...v,
              venueId: num(v.venueId),
              performerId: v.performerId === "" ? null : num(v.performerId),
              endTime: v.endTime === "" ? null : v.endTime,
              basePrice: v.basePrice,
              ticketCount: Number(v.ticketCount),
            }));
          }}
        >
          <input placeholder="name" value={event.name} onChange={(e) => setEvent({ ...event, name: e.target.value })} />
          <input placeholder="description" value={event.description} onChange={(e) => setEvent({ ...event, description: e.target.value })} />
          <input placeholder="type: MOVIE, COMEDY, CONCERT…" value={event.type} onChange={(e) => setEvent({ ...event, type: e.target.value })} />
          <input placeholder="venueId" value={event.venueId} onChange={(e) => setEvent({ ...event, venueId: e.target.value })} />
          <input placeholder="performerId (optional)" value={event.performerId} onChange={(e) => setEvent({ ...event, performerId: e.target.value })} />
          <input placeholder="startTime 2026-11-01T20:00:00" value={event.startTime} onChange={(e) => setEvent({ ...event, startTime: e.target.value })} />
          <input placeholder="endTime (optional)" value={event.endTime} onChange={(e) => setEvent({ ...event, endTime: e.target.value })} />
          <input placeholder="basePrice" value={event.basePrice} onChange={(e) => setEvent({ ...event, basePrice: e.target.value })} />
          <input placeholder="ticketCount" value={event.ticketCount} onChange={(e) => setEvent({ ...event, ticketCount: e.target.value })} />
          <button type="submit">Create event</button>
        </form>
      </Section>
      <Section title="Create scheduled run (daily shows)">
        <p className="muted">
          Same show on each day from start to end date, at the given start
          times (HH:MM, comma separated) with the given duration. Times must
          not overlap.
        </p>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            submit(api.createSchedule, run, (v) => ({
              name: v.name,
              description: v.description,
              type: v.type,
              venueId: num(v.venueId),
              performerId: v.performerId === "" ? null : num(v.performerId),
              basePrice: v.basePrice,
              ticketCount: Number(v.ticketCount),
              schedule: {
                startDate: v.startDate,
                endDate: v.endDate,
                showTimes: v.showTimes.split(",").map((s) => s.trim()).filter(Boolean),
                durationMinutes: Number(v.durationMinutes),
              },
            }));
          }}
        >
          <input placeholder="name" value={run.name} onChange={(e) => setRun({ ...run, name: e.target.value })} />
          <input placeholder="description" value={run.description} onChange={(e) => setRun({ ...run, description: e.target.value })} />
          <input placeholder="type: MOVIE, COMEDY, CONCERT…" value={run.type} onChange={(e) => setRun({ ...run, type: e.target.value })} />
          <input placeholder="venueId" value={run.venueId} onChange={(e) => setRun({ ...run, venueId: e.target.value })} />
          <input placeholder="performerId (optional)" value={run.performerId} onChange={(e) => setRun({ ...run, performerId: e.target.value })} />
          <input placeholder="basePrice" value={run.basePrice} onChange={(e) => setRun({ ...run, basePrice: e.target.value })} />
          <input placeholder="ticketCount per show" value={run.ticketCount} onChange={(e) => setRun({ ...run, ticketCount: e.target.value })} />
          <input placeholder="startDate 2026-11-03" value={run.startDate} onChange={(e) => setRun({ ...run, startDate: e.target.value })} />
          <input placeholder="endDate 2026-11-09" value={run.endDate} onChange={(e) => setRun({ ...run, endDate: e.target.value })} />
          <input placeholder="showTimes, e.g. 08:00, 09:30" value={run.showTimes} onChange={(e) => setRun({ ...run, showTimes: e.target.value })} />
          <input placeholder="durationMinutes, e.g. 90" value={run.durationMinutes} onChange={(e) => setRun({ ...run, durationMinutes: e.target.value })} />
          <button type="submit">Create scheduled run</button>
        </form>
      </Section>
      <Result result={result} error={error} />
    </div>
  );
}
