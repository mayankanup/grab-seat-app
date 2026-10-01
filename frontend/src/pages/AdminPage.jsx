import { useEffect, useState } from "react";
import { api } from "../api";

const TYPES = ["MOVIE", "COMEDY", "CONCERT", "SPORTS", "THEATER"];
const DURATIONS = [60, 90, 120, 180];

function Section({ title, children }) {
  return (
    <div className="card">
      <h3>{title}</h3>
      {children}
    </div>
  );
}

function Field({ label, hint, children }) {
  return (
    <label className="field">
      <span>
        {label} {hint && <small className="muted">{hint}</small>}
      </span>
      {children}
    </label>
  );
}

function Notice({ result, error }) {
  if (error) return <p className="error">{error}</p>;
  if (!result) return null;
  return <p className="success">{result}</p>;
}

const emptyVenue = { name: "", location: "", capacity: 100 };
const emptyPerformer = { name: "", type: "COMEDIAN" };
const emptyEvent = {
  name: "",
  description: "",
  type: "COMEDY",
  venueId: "",
  performerId: "",
  screenId: "",
  startTime: "",
  endTime: "",
  basePrice: "",
  ticketCount: 40,
};
const emptyRun = {
  name: "",
  description: "",
  type: "MOVIE",
  venueId: "",
  performerId: "",
  basePrice: "",
  ticketCount: 40,
  startDate: "",
  endDate: "",
  showTimes: [],
  newTime: "08:00",
  durationMinutes: 90,
};

function minutesToTime(total) {
  const h = String(Math.floor(total / 60) % 24).padStart(2, "0");
  const m = String(total % 60).padStart(2, "0");
  return `${h}:${m}`;
}

function slotEnd(start, durationMinutes) {
  const [h, m] = start.split(":").map(Number);
  return minutesToTime(h * 60 + m + durationMinutes);
}

function overlapping(times, durationMinutes) {
  const sorted = [...times].sort();
  for (let i = 1; i < sorted.length; i++) {
    if (slotEnd(sorted[i - 1], durationMinutes) > sorted[i]) return true;
  }
  return false;
}

function dayCount(startDate, endDate) {
  if (!startDate || !endDate) return 0;
  const days =
    Math.round((new Date(endDate) - new Date(startDate)) / 86400000) + 1;
  return days > 0 ? days : 0;
}

export default function AdminPage() {
  const [venues, setVenues] = useState([]);
  const [performers, setPerformers] = useState([]);
  const [shows, setShows] = useState([]);
  const [screens, setScreens] = useState([]);
  const [screen, setScreen] = useState({ venueId: "", name: "", capacity: 50 });
  const [venue, setVenue] = useState(emptyVenue);
  const [performer, setPerformer] = useState(emptyPerformer);
  const [event, setEvent] = useState(emptyEvent);
  const [run, setRun] = useState(emptyRun);
  const [editing, setEditing] = useState({ venue: null, performer: null, event: null, screen: null });
  const [result, setResult] = useState("");
  const [error, setError] = useState("");

  const num = (v) => (v === "" ? null : Number(v));

  async function refresh() {
    try {
      const [v, p, e, s] = await Promise.all([
        api.listVenues(),
        api.listPerformers(),
        api.listEvents(0, 50),
        api.listScreens(),
      ]);
      setVenues(v);
      setPerformers(p);
      setShows(e.content ?? []);
      setScreens(s);
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function saved(message, work) {
    setError("");
    setResult("");
    try {
      await work();
      setResult(message);
      setEditing({ venue: null, performer: null, event: null, screen: null });
      setVenue(emptyVenue);
      setPerformer(emptyPerformer);
      setEvent(emptyEvent);
      setScreen({ venueId: "", name: "", capacity: 50 });
      await refresh();
    } catch (err) {
      setError(err.message);
    }
  }

  function editVenue(v) {
    setEditing({ venue: v.id, performer: null, event: null, screen: null });
    setVenue({ name: v.name, location: v.location, capacity: v.capacity });
  }

  function editPerformer(p) {
    setEditing({ venue: null, performer: p.id, event: null, screen: null });
    setPerformer({ name: p.name, type: p.type });
  }

  function editScreen(s) {
    setEditing({ venue: null, performer: null, event: null, screen: s.id });
    setScreen({ venueId: String(s.venueId), name: s.name, capacity: s.capacity });
  }

  async function editEvent(id) {
    setError("");
    try {
      const full = await api.getEvent(id);
      setEditing({ venue: null, performer: null, event: id, screen: null });
      setEvent({
        name: full.name,
        description: full.description ?? "",
        type: full.type,
        venueId: String(full.venue?.id ?? ""),
        performerId: full.performer?.id ? String(full.performer.id) : "",
        screenId: full.screen?.id ? String(full.screen.id) : "",
        startTime: full.startTime?.slice(0, 16) ?? "",
        endTime: full.endTime?.slice(0, 16) ?? "",
        basePrice: full.basePrice,
        ticketCount: full.tickets?.length ?? 40,
      });
    } catch (err) {
      setError(err.message);
    }
  }

  function cancelEdit() {
    setEditing({ venue: null, performer: null, event: null, screen: null });
    setVenue(emptyVenue);
    setPerformer(emptyPerformer);
    setEvent(emptyEvent);
    setScreen({ venueId: "", name: "", capacity: 50 });
  }

  const days = dayCount(run.startDate, run.endDate);
  const showCount = days * run.showTimes.length;
  const clash = overlapping(run.showTimes, Number(run.durationMinutes) || 0);

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
              saved(`Venue #${editing.venue} saved.`, () =>
                api.updateVenue(editing.venue, payload)
              );
            } else {
              saved("Venue created.", () => api.createVenue(payload));
            }
          }}
        >
          <Field label={editing.venue ? `Name (editing #${editing.venue})` : "Name"}>
            <input required value={venue.name} onChange={(e) => setVenue({ ...venue, name: e.target.value })} />
          </Field>
          <Field label="Location">
            <input required value={venue.location} onChange={(e) => setVenue({ ...venue, location: e.target.value })} />
          </Field>
          <Field label="Capacity" hint="seats in the hall">
            <input required type="number" min="1" max="100000" value={venue.capacity} onChange={(e) => setVenue({ ...venue, capacity: e.target.value })} />
          </Field>
          <div className="row">
            <button type="submit">{editing.venue ? "Save venue" : "Create venue"}</button>
            {editing.venue && (
              <button type="button" onClick={cancelEdit}>Cancel</button>
            )}
          </div>
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
              saved(`Performer #${editing.performer} saved.`, () =>
                api.updatePerformer(editing.performer, performer)
              );
            } else {
              saved("Performer created.", () => api.createPerformer(performer));
            }
          }}
        >
          <Field label={editing.performer ? `Name (editing #${editing.performer})` : "Name"}>
            <input required value={performer.name} onChange={(e) => setPerformer({ ...performer, name: e.target.value })} />
          </Field>
          <Field label="Type" hint="e.g. COMEDIAN, BAND, CAST">
            <input required value={performer.type} onChange={(e) => setPerformer({ ...performer, type: e.target.value })} />
          </Field>
          <div className="row">
            <button type="submit">{editing.performer ? "Save performer" : "Create performer"}</button>
            {editing.performer && (
              <button type="button" onClick={cancelEdit}>Cancel</button>
            )}
          </div>
        </form>
      </Section>
      <Section title="Screens">
        <ul>
          {screens.map((s) => (
            <li key={s.id}>
              #{s.id} {s.name} · {s.venueName} · cap {s.capacity}{" "}
              <button onClick={() => editScreen(s)}>Edit</button>
            </li>
          ))}
        </ul>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const payload = {
              venueId: num(screen.venueId),
              name: screen.name,
              capacity: Number(screen.capacity),
            };
            if (editing.screen) {
              saved(`Screen #${editing.screen} saved.`, () =>
                api.updateScreen(editing.screen, payload)
              );
            } else {
              saved("Screen created.", () => api.createScreen(payload));
            }
          }}
        >
          <Field label={editing.screen ? `Name (editing #${editing.screen})` : "Name"} hint="e.g. IMAX, Screen 4">
            <input required value={screen.name} onChange={(e) => setScreen({ ...screen, name: e.target.value })} />
          </Field>
          <Field label="Venue">
            <select required value={screen.venueId} onChange={(e) => setScreen({ ...screen, venueId: e.target.value })}>
              <option value="">— choose venue —</option>
              {venues.map((v) => (
                <option key={v.id} value={v.id}>#{v.id} {v.name}</option>
              ))}
            </select>
          </Field>
          <Field label="Capacity" hint="seats in this screen">
            <input required type="number" min="1" max="100000" value={screen.capacity} onChange={(e) => setScreen({ ...screen, capacity: e.target.value })} />
          </Field>
          <div className="row">
            <button type="submit">{editing.screen ? "Save screen" : "Create screen"}</button>
            {editing.screen && (
              <button type="button" onClick={cancelEdit}>Cancel</button>
            )}
          </div>
        </form>
      </Section>
      <Section title="Events (first 50)">
        <ul>
          {shows.map((s) => (
            <li key={s.id}>
              #{s.id} {s.name} · {s.venueName}
              {s.screenName && <> · {s.screenName}</>}{" "}
              <button onClick={() => editEvent(s.id)}>Edit</button>
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
              screenId: event.screenId === "" ? null : num(event.screenId),
              endTime: event.endTime === "" ? null : event.endTime,
            };
            if (editing.event) {
              const { ticketCount: _drop, ...update } = payload;
              saved(`Event #${editing.event} saved.`, () =>
                api.updateEvent(editing.event, update)
              );
            } else {
              saved("Event created.", () =>
                api.createEvent({ ...payload, ticketCount: Number(event.ticketCount) })
              );
            }
          }}
        >
          <Field label={editing.event ? `Name (editing #${editing.event})` : "Name"}>
            <input required value={event.name} onChange={(e) => setEvent({ ...event, name: e.target.value })} />
          </Field>
          <Field label="Description">
            <input value={event.description} onChange={(e) => setEvent({ ...event, description: e.target.value })} />
          </Field>
          <Field label="Type">
            <select value={event.type} onChange={(e) => setEvent({ ...event, type: e.target.value })}>
              {TYPES.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </Field>
          <Field label="Venue" hint="pick the hall, not its id">
            <select required value={event.venueId} onChange={(e) => setEvent({ ...event, venueId: e.target.value })}>
              <option value="">— choose venue —</option>
              {venues.map((v) => (
                <option key={v.id} value={v.id}>#{v.id} {v.name}</option>
              ))}
            </select>
          </Field>
          <Field label="Performer" hint="optional">
            <select value={event.performerId} onChange={(e) => setEvent({ ...event, performerId: e.target.value })}>
              <option value="">— none —</option>
              {performers.map((p) => (
                <option key={p.id} value={p.id}>#{p.id} {p.name}</option>
              ))}
            </select>
          </Field>
          <Field label="Screen" hint="optional; shows in this hall">
            <select value={event.screenId} onChange={(e) => setEvent({ ...event, screenId: e.target.value })}>
              <option value="">— none —</option>
              {screens
                .filter((s) => event.venueId === "" || String(s.venueId) === String(event.venueId))
                .map((s) => (
                  <option key={s.id} value={s.id}>{s.name} (cap {s.capacity})</option>
                ))}
            </select>
          </Field>
          <Field label="Starts">
            <input required type="datetime-local" value={event.startTime} onChange={(e) => setEvent({ ...event, startTime: e.target.value })} />
          </Field>
          <Field label="Ends" hint="optional">
            <input type="datetime-local" min={event.startTime || undefined} value={event.endTime} onChange={(e) => setEvent({ ...event, endTime: e.target.value })} />
          </Field>
          <Field label="Base price (₹)">
            <input required type="number" min="1" step="0.01" value={event.basePrice} onChange={(e) => setEvent({ ...event, basePrice: e.target.value })} />
          </Field>
          {!editing.event && (
            <Field label="Tickets per show">
              <input required type="number" min="1" max="1000" value={event.ticketCount} onChange={(e) => setEvent({ ...event, ticketCount: e.target.value })} />
            </Field>
          )}
          <div className="row">
            <button type="submit">{editing.event ? "Save event" : "Create event"}</button>
            {editing.event && (
              <button type="button" onClick={cancelEdit}>Cancel</button>
            )}
          </div>
        </form>
      </Section>
      <Section title="Create scheduled run (daily shows)">
        <p className="muted">
          Repeats one show every day from start to end date. Existing ticket
          prices on edited shows are left untouched.
        </p>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (clash || showCount === 0) {
              setError("Fix the schedule: need valid dates and non-overlapping times.");
              return;
            }
            saved(`${showCount} shows will be created.`, () =>
              api
                .createSchedule({
                  name: run.name,
                  description: run.description,
                  type: run.type,
                  venueId: num(run.venueId),
                  performerId: run.performerId === "" ? null : num(run.performerId),
                  screenId: run.screenId === "" ? null : num(run.screenId),
                  basePrice: run.basePrice,
                  ticketCount: Number(run.ticketCount),
                  schedule: {
                    startDate: run.startDate,
                    endDate: run.endDate,
                    showTimes: [...run.showTimes].sort(),
                    durationMinutes: Number(run.durationMinutes),
                  },
                })
                .then((created) => `${created.length} shows created.`)
            );
          }}
        >
          <Field label="Show name">
            <input required value={run.name} onChange={(e) => setRun({ ...run, name: e.target.value })} />
          </Field>
          <Field label="Description">
            <input value={run.description} onChange={(e) => setRun({ ...run, description: e.target.value })} />
          </Field>
          <Field label="Type">
            <select value={run.type} onChange={(e) => setRun({ ...run, type: e.target.value })}>
              {TYPES.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </Field>
          <Field label="Venue">
            <select required value={run.venueId} onChange={(e) => setRun({ ...run, venueId: e.target.value })}>
              <option value="">— choose venue —</option>
              {venues.map((v) => (
                <option key={v.id} value={v.id}>#{v.id} {v.name}</option>
              ))}
            </select>
          </Field>
          <Field label="Performer" hint="optional">
            <select value={run.performerId} onChange={(e) => setRun({ ...run, performerId: e.target.value })}>
              <option value="">— none —</option>
              {performers.map((p) => (
                <option key={p.id} value={p.id}>#{p.id} {p.name}</option>
              ))}
            </select>
          </Field>
          <Field label="Screen" hint="optional; clash-checked per screen">
            <select value={run.screenId} onChange={(e) => setRun({ ...run, screenId: e.target.value })}>
              <option value="">— none —</option>
              {screens
                .filter((s) => run.venueId === "" || String(s.venueId) === String(run.venueId))
                .map((s) => (
                  <option key={s.id} value={s.id}>{s.name} (cap {s.capacity})</option>
                ))}
            </select>
          </Field>
          <Field label="Base price (₹)">
            <input required type="number" min="1" step="0.01" value={run.basePrice} onChange={(e) => setRun({ ...run, basePrice: e.target.value })} />
          </Field>
          <Field label="Tickets per show">
            <input required type="number" min="1" max="1000" value={run.ticketCount} onChange={(e) => setRun({ ...run, ticketCount: e.target.value })} />
          </Field>
          <Field label="First day">
            <input required type="date" value={run.startDate} onChange={(e) => setRun({ ...run, startDate: e.target.value })} />
          </Field>
          <Field label="Last day">
            <input required type="date" min={run.startDate || undefined} value={run.endDate} onChange={(e) => setRun({ ...run, endDate: e.target.value })} />
          </Field>
          <Field label="Show start times" hint="tap + to add a slot">
            <div className="row">
              <input
                type="time"
                value={run.newTime}
                onChange={(e) => setRun({ ...run, newTime: e.target.value })}
              />
              <button
                type="button"
                onClick={() =>
                  run.newTime &&
                  !run.showTimes.includes(run.newTime) &&
                  setRun({ ...run, showTimes: [...run.showTimes, run.newTime] })
                }
              >
                + Add
              </button>
            </div>
          </Field>
          {run.showTimes.length > 0 && (
            <ul>
              {[...run.showTimes].sort().map((t) => (
                <li key={t}>
                  {t} – {slotEnd(t, Number(run.durationMinutes) || 0)}{" "}
                  <button
                    type="button"
                    onClick={() =>
                      setRun({ ...run, showTimes: run.showTimes.filter((x) => x !== t) })
                    }
                  >
                    Remove
                  </button>
                </li>
              ))}
            </ul>
          )}
          <Field label="Duration (minutes)">
            <div className="row">
              {DURATIONS.map((d) => (
                <button
                  key={d}
                  type="button"
                  className={Number(run.durationMinutes) === d ? "picked" : ""}
                  onClick={() => setRun({ ...run, durationMinutes: d })}
                >
                  {d}
                </button>
              ))}
              <input
                type="number"
                min="15"
                max="600"
                value={run.durationMinutes}
                onChange={(e) => setRun({ ...run, durationMinutes: e.target.value })}
              />
            </div>
          </Field>
          {showCount > 0 && (
            <p className={clash ? "error" : "muted"}>
              {days} day(s) × {run.showTimes.length} slot(s) = {showCount} show(s).
              {clash && " Slots overlap for this duration — adjust times."}
            </p>
          )}
          <button type="submit" disabled={showCount === 0 || clash}>
            Create scheduled run
          </button>
        </form>
      </Section>
      <Notice result={result} error={error} />
    </div>
  );
}
