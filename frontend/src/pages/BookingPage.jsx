import { Link, useLocation, useNavigate } from "react-router-dom";

export default function BookingPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const reservation = location.state?.reservation;

  if (!reservation) {
    return (
      <div className="card narrow">
        <h2>No booking here yet</h2>
        <p className="muted">
          Reservations live here right after you reserve seats (saved booking
          history arrives with US5).
        </p>
        <Link to="/">Browse events</Link>
      </div>
    );
  }

  const total = reservation.tickets.reduce(
    (sum, t) => sum + Number(t.price),
    0
  );

  return (
    <div className="card">
      <h2>Booking #{reservation.bookingId}</h2>
      <p className="muted">
        Status {reservation.bookingStatus} · held for {reservation.userId}
      </p>
      <table>
        <thead>
          <tr>
            <th>Seat</th>
            <th>Price</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {reservation.tickets.map((t) => (
            <tr key={t.ticketId}>
              <td>{t.seatNumber}</td>
              <td>₹{t.price}</td>
              <td>{t.ticketStatus}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="row">
        <p>
          Total <strong>₹{total.toFixed(2)}</strong>
        </p>
        <button
          onClick={() =>
            navigate("/payment", { state: { reservation } })
          }
        >
          Proceed to payment
        </button>
      </div>
    </div>
  );
}
