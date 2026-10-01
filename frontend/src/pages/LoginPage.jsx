import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth";

export default function LoginPage() {
  const [id, setId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const next = location.state?.next ?? "/";

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await login(id.trim(), password);
      navigate(next, { replace: true });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="card narrow">
      <h2>Login</h2>
      <form onSubmit={submit}>
        <input
          placeholder="login, e.g. anup"
          value={id}
          onChange={(e) => setId(e.target.value)}
        />
        <input
          type="password"
          placeholder="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button type="submit">Login</button>
      </form>
      {error && <p className="error">{error}</p>}
      <p className="muted">
        New here? <Link to="/register">Create an account</Link> · Browsing is
        public — <Link to="/">back to events</Link>.
      </p>
    </div>
  );
}
