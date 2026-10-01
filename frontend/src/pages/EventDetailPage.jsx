import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api, isTokenValid } from "../api";

export default function EventDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState(null);
  const [selected, setSelected] = useState([]);
  const [error, setError] = useState("");
  const [reserving, setReserving] = useState(false);

  useEffect(() => {
    api
      .getEvent(id)
      .then((ev) => {
        setEvent(ev);
        const pending = JSON.parse(
          sessionStorage.getItem("pendingSeats") ?? "null"
        );
        if (pending && String(pending.eventId) === String(id)) {
          setSelected(
            pending.seatIds.filter((t) =>
              ev.tickets.some((x) => x.id === t && x.status === "AVAILABLE")
            )
          );
          sessionStorage.removeItem("pendingSeats");
        }
      })
      .catch((err) => setError(err.message));
  }, [id]);

  function toggle(ticketId) {
    setSelected((s) =>
      s.includes(ticketId) ? s.filter((t) => t !== ticketId) : [...s, ticketId]
    );
  }

  async function reserve() {
    setError("");
    if (!isTokenValid()) {
      sessionStorage.setItem(
        "pendingSeats",
        JSON.stringify({ eventId: id, seatIds: selected })
      );
      navigate("/login", { state: { next: `/events/${id}` } });
      return;
    }
    setReserving(true);
    try {
      const reservation = await api.reserve(selected);
      navigate("/booking", { state: { reservation } });
    } catch (err) {
      setError(err.message);
    } finally {
      setReserving(false);
    }
  }

  if (error && !event) return <p className="error">{error}</p>;
  if (!event) return <p className="muted">Loading…</p>;

  const total = event.tickets
    .filter((t) => selected.includes(t.id))
    .reduce((sum, t) => sum + Number(t.price), 0);

  return (
    <div>
      <span className="badge">{event.type}</span>
      <h2>{event.name}</h2>
      <p className="muted">
        {new Date(event.startTime).toLocaleString()} · {event.venue?.name} (
        {event.venue?.location}) · {event.performer?.name}
      </p>
      <p>{event.description}</p>
      <h3>Select seats</h3>
      <div className="seats">
        {event.tickets.map((t) => (
          <button
            key={t.id}
            disabled={t.status !== "AVAILABLE"}
            className={`seat ${t.status !== "AVAILABLE" ? "taken" : ""} ${
              selected.includes(t.id) ? "picked" : ""
            }`}
            onClick={() => toggle(t.id)}
            title={`${t.seatNumber} · ₹${t.price} · ${t.status}`}
          >
            {t.seatNumber}
          </button>
        ))}
      </div>
      {error && <p className="error">{error}</p>}
      <div className="row">
        <p>
          {selected.length} seat(s) · <strong>₹{total.toFixed(2)}</strong>
        </p>
        <button disabled={selected.length === 0 || reserving} onClick={reserve}>
          {reserving ? "Reserving…" : "Reserve seats"}
        </button>
      </div>
    </div>
  );
}
