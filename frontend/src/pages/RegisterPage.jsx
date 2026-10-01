import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth";

export default function RegisterPage() {
  const [id, setId] = useState("");
  const [password, setPassword] = useState("");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const { register } = useAuth();
  const navigate = useNavigate();

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await register(id.trim(), password, fullName.trim(), email.trim());
      navigate("/", { replace: true });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="card narrow">
      <h2>Create account</h2>
      <p className="muted">
        Login: 3–32 chars (letters, digits, . _ -). Password: min 8 chars.
        Booking confirmations go to your email.
      </p>
      <form onSubmit={submit}>
        <input
          placeholder="login, e.g. anup"
          value={id}
          onChange={(e) => setId(e.target.value)}
        />
        <input
          placeholder="full name, e.g. Anup Kumar"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
        />
        <input
          type="email"
          placeholder="email, e.g. anup@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          type="password"
          placeholder="password (min 8 chars)"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button type="submit">Register</button>
      </form>
      {error && <p className="error">{error}</p>}
      <p className="muted">
        Already have one? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}
