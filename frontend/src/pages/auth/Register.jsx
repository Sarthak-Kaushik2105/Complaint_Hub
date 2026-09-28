import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  FaEnvelope,
  FaLock,
  FaUser,
  FaUserPlus,
} from "react-icons/fa";
import "./Register.css";

const REGISTERED_USERS_KEY = "registeredUsers";

function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    const trimmedName = name.trim();
    const trimmedEmail = email.trim().toLowerCase();

    if (!trimmedName || !trimmedEmail || !password || !confirmPassword) {
      setError("Please fill in all fields.");
      return;
    }

    if (password.length < 6) {
      setError("Password must be at least 6 characters long.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    const registeredUsers = JSON.parse(
      localStorage.getItem(REGISTERED_USERS_KEY) || "[]"
    );

    const existingUser = registeredUsers.find(
      (user) => user.email.toLowerCase() === trimmedEmail
    );

    if (existingUser) {
      setError("An account with this email already exists.");
      return;
    }

    const newUser = {
      id: `USR-${Date.now()}`,
      name: trimmedName,
      email: trimmedEmail,
      password,
      role: "user",
      createdAt: new Date().toISOString(),
    };

    localStorage.setItem(
      REGISTERED_USERS_KEY,
      JSON.stringify([...registeredUsers, newUser])
    );

    setSuccess("Account created successfully. Redirecting to login...");

    setTimeout(() => {
      navigate("/login");
    }, 1200);
  };

  return (
    <div className="register-page">
      <div className="register-card">

        <div className="register-logo">
          <div className="logo-box">C</div>

          <div>
            <h2>ComplaintHub</h2>
            <p>Service Portal</p>
          </div>
        </div>

        <div className="register-heading">
          <div className="register-icon">
            <FaUserPlus />
          </div>

          <div>
            <h1>Create your account</h1>
            <p>Register as a user to access the complaint portal.</p>
          </div>
        </div>

        {error && (
          <div className="register-message error-message">
            {error}
          </div>
        )}

        {success && (
          <div className="register-message success-message">
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit}>

          <div className="register-input-group">
            <FaUser className="register-input-icon" />

            <input
              type="text"
              placeholder="Full name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

          <div className="register-input-group">
            <FaEnvelope className="register-input-icon" />

            <input
              type="email"
              placeholder="Email address"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="register-input-group">
            <FaLock className="register-input-icon" />

            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <div className="register-input-group">
            <FaLock className="register-input-icon" />

            <input
              type="password"
              placeholder="Confirm password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>

          <div className="user-role-info">
            <span>Account type</span>
            <strong>User</strong>
          </div>

          <button type="submit" className="register-btn">
            Register as User
          </button>
        </form>

        <p className="register-footer">
          Already have an account?{" "}
          <Link to="/login">Sign in</Link>
        </p>

      </div>
    </div>
  );
}

export default Register;