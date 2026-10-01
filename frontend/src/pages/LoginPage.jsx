import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth";

export default function LoginPage() {
  const [id, setId] = useState("");
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const next = location.state?.next ?? "/";

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await login(id.trim());
      navigate(next, { replace: true });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="card narrow">
      <h2>Login</h2>
      <p className="muted">Dev login — any user id gets a JWT (passwords arrive later).</p>
      <form onSubmit={submit}>
        <input
          placeholder="user id, e.g. anup"
          value={id}
          onChange={(e) => setId(e.target.value)}
        />
        <button type="submit">Login</button>
      </form>
      {error && <p className="error">{error}</p>}
      <p className="muted">
        Browsing is public — <Link to="/">back to events</Link>.
      </p>
    </div>
  );
}
