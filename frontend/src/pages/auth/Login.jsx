import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaEnvelope, FaLock, FaUser, FaUserTie, FaUserShield } from "react-icons/fa";
import "./Login.css";

const ROLES = [
  { key: "user", label: "User", icon: <FaUser />, path: "/customer/complaints" },
  { key: "agent", label: "Agent", icon: <FaUserTie />, path: "/agent/dashboard" },
  { key: "admin", label: "Admin", icon: <FaUserShield />, path: "/admin/dashboard" },
];

function Login() {
  const navigate = useNavigate();
  const [role, setRole] = useState("user");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    // Placeholder only — real authentication will call your backend API here.
    localStorage.setItem("role", role);
    const selected = ROLES.find((r) => r.key === role);
    navigate(selected.path);
  };

  const activeLabel = ROLES.find((r) => r.key === role).label;

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">
          <div className="logo-box">C</div>
          <div>
            <h2>ComplaintHub</h2>
            <p>Service Portal</p>
          </div>
        </div>

        <h1>Welcome back</h1>
        <p className="login-subtitle">Sign in to continue to your dashboard</p>

        <div className="role-toggle">
          {ROLES.map((r) => (
            <button
              type="button"
              key={r.key}
              className={role === r.key ? "role-btn active" : "role-btn"}
              onClick={() => setRole(r.key)}
            >
              {r.icon}
              <span>{r.label}</span>
            </button>
          ))}
        </div>

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <FaEnvelope className="input-icon" />
            <input
              type="email"
              placeholder="Email address"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="input-group">
            <FaLock className="input-icon" />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="login-btn">
            Sign In as {activeLabel}
          </button>
        </form>

        <p className="login-footer">
          Don't have an account? <a href="/register">Register as a user</a>
        </p>
      </div>
    </div>
  );
}

export default Login;