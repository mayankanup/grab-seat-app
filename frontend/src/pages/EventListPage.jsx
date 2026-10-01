import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";

function fmt(date) {
  return date ? new Date(date).toLocaleString() : "—";
}

export default function EventListPage() {
  const [keyword, setKeyword] = useState("");
  const [query, setQuery] = useState("");
  const [events, setEvents] = useState([]);
  const [total, setTotal] = useState(0);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    api
      .searchEvents(query)
      .then((page) => {
        if (!cancelled) {
          setEvents(page.content ?? []);
          setTotal(page.totalElements ?? 0);
        }
      })
      .catch((err) => !cancelled && setError(err.message));
    return () => {
      cancelled = true;
    };
  }, [query]);

  return (
    <div>
      <form
        className="row"
        onSubmit={(e) => {
          e.preventDefault();
          setQuery(keyword.trim());
        }}
      >
        <input
          placeholder="Search movies, comedy…"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <button type="submit">Search</button>
      </form>
      {error && <p className="error">{error}</p>}
      <p className="muted">{total} event(s)</p>
      <div className="grid">
        {events.map((ev) => (
          <Link key={ev.id} to={`/events/${ev.id}`} className="card link">
            <span className="badge">{ev.type}</span>
            <h3>{ev.name}</h3>
            <p className="muted">
              {fmt(ev.startTime)} · {ev.venueName}
              {ev.screenName && <> · {ev.screenName}</>}
            </p>
            <p>
              {ev.performerName} · ₹{ev.basePrice}
            </p>
          </Link>
        ))}
      </div>
    </div>
  );
}
