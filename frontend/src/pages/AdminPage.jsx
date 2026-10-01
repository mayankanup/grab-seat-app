import { useEffect, useState } from "react";
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

const emptyVenue = { name: "", location: "", capacity: 100 };
const emptyPerformer = { name: "", type: "COMEDIAN" };
const emptyEvent = {
  name: "",
  description: "",
  type: "COMEDY",
  venueId: "",
  performerId: "",
  startTime: "",
  endTime: "",
  basePrice: "",
  ticketCount: 40,
};

export default function AdminPage() {
  const [venues, setVenues] = useState([]);
  const [performers, setPerformers] = useState([]);
  const [shows, setShows] = useState([]);
  const [venue, setVenue] = useState(emptyVenue);
  const [performer, setPerformer] = useState(emptyPerformer);
  const [event, setEvent] = useState(emptyEvent);
  const [editing, setEditing] = useState({ venue: null, performer: null, event: null });
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
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const num = (v) => (v === "" ? null : Number(v));

  async function refresh() {
    try {
      const [v, p, e] = await Promise.all([
        api.listVenues(),
        api.listPerformers(),
        api.listEvents(0, 50),
      ]);
      setVenues(v);
      setPerformers(p);
      setShows(e.content ?? []);
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function save(kind, payload) {
    setError("");
    setResult(null);
    try {
      const res = await kind.fn(...kind.args(payload));
      setResult(res);
      setEditing({ venue: null, performer: null, event: null });
      setVenue(emptyVenue);
      setPerformer(emptyPerformer);
      setEvent(emptyEvent);
      await refresh();
    } catch (err) {
      setError(err.message);
    }
  }

  function editVenue(v) {
    setEditing({ venue: v.id, performer: null, event: null });
    setVenue({ name: v.name, location: v.location, capacity: v.capacity });
  }

  function editPerformer(p) {
    setEditing({ venue: null, performer: p.id, event: null });
    setPerformer({ name: p.name, type: p.type });
  }

  function editEvent(e) {
    setEditing({ venue: null, performer: null, event: e.id });
    setEvent({
      name: e.name,
      description: e.description ?? "",
      type: e.type,
      venueId: "",
      performerId: "",
      startTime: e.startTime?.slice(0, 19) ?? "",
      endTime: e.endTime?.slice(0, 19) ?? "",
      basePrice: e.basePrice,
      ticketCount: 40,
    });
  }

  return (
    <div>
      <h2>Admin</h2>
      <Section title="Venues">
        <ul>
          {venues.map((v) => (
            <li key={v.id}>
              #{v.id} {v.name} · {v.location} · cap {v.capacity}{" "}
              <button onClick={() => editVenue(v)}>Edit</button>
            </li>
          ))}
        </ul>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const payload = { ...venue, capacity: Number(venue.capacity) };
            if (editing.venue) {
              save({ fn: api.updateVenue, args: [editing.venue] }, payload);
            } else {
              save({ fn: api.createVenue, args: [] }, payload);
            }
          }}
        >
          <input placeholder="name" value={venue.name} onChange={(e) => setVenue({ ...venue, name: e.target.value })} />
          <input placeholder="location" value={venue.location} onChange={(e) => setVenue({ ...venue, location: e.target.value })} />
          <input placeholder="capacity" value={venue.capacity} onChange={(e) => setVenue({ ...venue, capacity: e.target.value })} />
          <button type="submit">{editing.venue ? `Save venue #${editing.venue}` : "Create venue"}</button>
        </form>
      </Section>
      <Section title="Performers">
        <ul>
          {performers.map((p) => (
            <li key={p.id}>
              #{p.id} {p.name} · {p.type}{" "}
              <button onClick={() => editPerformer(p)}>Edit</button>
            </li>
          ))}
        </ul>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (editing.performer) {
              save({ fn: api.updatePerformer, args: [editing.performer] }, performer);
            } else {
              save({ fn: api.createPerformer, args: [] }, performer);
            }
          }}
        >
          <input placeholder="name" value={performer.name} onChange={(e) => setPerformer({ ...performer, name: e.target.value })} />
          <input placeholder="type, e.g. COMEDIAN" value={performer.type} onChange={(e) => setPerformer({ ...performer, type: e.target.value })} />
          <button type="submit">{editing.performer ? `Save performer #${editing.performer}` : "Create performer"}</button>
        </form>
      </Section>
      <Section title="Events (first 50)">
        <ul>
          {shows.map((s) => (
            <li key={s.id}>
              #{s.id} {s.name} · {s.venueName}{" "}
              <button onClick={() => editEvent(s)}>Edit</button>
            </li>
          ))}
        </ul>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const payload = {
              ...event,
              venueId: num(event.venueId),
              performerId: event.performerId === "" ? null : num(event.performerId),
              endTime: event.endTime === "" ? null : event.endTime,
            };
            if (editing.event) {
              const { ticketCount: _drop, ...update } = payload;
              save({ fn: api.updateEvent, args: [editing.event] }, update);
            } else {
              save({ fn: api.createEvent, args: [] }, { ...payload, ticketCount: Number(event.ticketCount) });
            }
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
          {!editing.event && (
            <input placeholder="ticketCount" value={event.ticketCount} onChange={(e) => setEvent({ ...event, ticketCount: e.target.value })} />
          )}
          <button type="submit">{editing.event ? `Save event #${editing.event}` : "Create event"}</button>
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
            save({ fn: api.createSchedule, args: [] }, {
              name: run.name,
              description: run.description,
              type: run.type,
              venueId: num(run.venueId),
              performerId: run.performerId === "" ? null : num(run.performerId),
              basePrice: run.basePrice,
              ticketCount: Number(run.ticketCount),
              schedule: {
                startDate: run.startDate,
                endDate: run.endDate,
                showTimes: run.showTimes.split(",").map((s) => s.trim()).filter(Boolean),
                durationMinutes: Number(run.durationMinutes),
              },
            });
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
          <input placeholder="showTimes, e.g. 08:00, 10:00" value={run.showTimes} onChange={(e) => setRun({ ...run, showTimes: e.target.value })} />
          <input placeholder="durationMinutes, e.g. 90" value={run.durationMinutes} onChange={(e) => setRun({ ...run, durationMinutes: e.target.value })} />
          <button type="submit">Create scheduled run</button>
        </form>
      </Section>
      <Result result={result} error={error} />
    </div>
  );
}
