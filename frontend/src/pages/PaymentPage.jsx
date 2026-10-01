import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { api } from "../api";

export default function PaymentPage() {
  const location = useLocation();
  const reservation = location.state?.reservation;
  const [cardNumber, setCardNumber] = useState("4242424242424242");
  const [expMonth, setExpMonth] = useState("12");
  const [expYear, setExpYear] = useState("2030");
  const [cvc, setCvc] = useState("123");
  const [error, setError] = useState("");
  const [paying, setPaying] = useState(false);
  const [receipt, setReceipt] = useState(null);

  if (!reservation) {
    return (
      <div className="card narrow">
        <h2>No booking to pay for</h2>
        <Link to="/">Browse events</Link>
      </div>
    );
  }

  if (receipt) {
    return (
      <div className="card">
        <h2>Payment confirmed 🎉</h2>
        <p>
          Booking #{receipt.bookingId} · {receipt.bookingStatus}
        </p>
        <p className="muted">Reference {receipt.paymentReference}</p>
        <ul>
          {receipt.tickets.map((t) => (
            <li key={t.ticketId}>
              {t.seatNumber} · ₹{t.price} · {t.ticketStatus}
            </li>
          ))}
        </ul>
        <Link to="/">Back to events</Link>
      </div>
    );
  }

  const total = reservation.tickets.reduce(
    (sum, t) => sum + Number(t.price),
    0
  );

  async function pay(e) {
    e.preventDefault();
    setError("");
    setPaying(true);
    try {
      const res = await api.confirm(reservation.bookingId, {
        cardNumber: cardNumber.replaceAll(" ", ""),
        expMonth: Number(expMonth),
        expYear: Number(expYear),
        cvc,
      });
      setReceipt(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setPaying(false);
    }
  }

  return (
    <div className="card narrow">
      <h2>Pay ₹{total.toFixed(2)}</h2>
      <p className="muted">
        Dummy Stripe — 4242 4242 4242 4242 approves, 4000 0000 0000 0002
        declines.
      </p>
      <form onSubmit={pay}>
        <input
          placeholder="Card number"
          value={cardNumber}
          onChange={(e) => setCardNumber(e.target.value)}
        />
        <div className="row">
          <input
            placeholder="MM"
            value={expMonth}
            onChange={(e) => setExpMonth(e.target.value)}
          />
          <input
            placeholder="YYYY"
            value={expYear}
            onChange={(e) => setExpYear(e.target.value)}
          />
          <input
            placeholder="CVC"
            value={cvc}
            onChange={(e) => setCvc(e.target.value)}
          />
        </div>
        <button type="submit" disabled={paying}>
          {paying ? "Paying…" : `Pay ₹${total.toFixed(2)}`}
        </button>
      </form>
      {error && <p className="error">{error}</p>}
    </div>
  );
}
