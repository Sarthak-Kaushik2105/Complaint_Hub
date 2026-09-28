import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  FaUserTie,
  FaUserShield,
  FaUser,
  FaEnvelope,
  FaLock,
  FaArrowLeft,
  FaUserPlus,
} from "react-icons/fa";
import "./StaffRegistration.css";

const REGISTERED_USERS_KEY = "registeredUsers";

function StaffRegistration() {
  const navigate = useNavigate();

  const [role, setRole] = useState("agent");
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
      setError(
        `An account with ${trimmedEmail} already exists as ${existingUser.role}.`
      );
      return;
    }

    const newStaff = {
      id: `${role === "agent" ? "AGT" : "ADM"}-${Date.now()}`,
      name: trimmedName,
      email: trimmedEmail,
      password,
      role,
      createdAt: new Date().toISOString(),
    };

    localStorage.setItem(
      REGISTERED_USERS_KEY,
      JSON.stringify([...registeredUsers, newStaff])
    );

    setSuccess(
      `${role === "agent" ? "Agent" : "Admin"} account created successfully.`
    );

    setName("");
    setEmail("");
    setPassword("");
    setConfirmPassword("");
  };

  return (
    <div className="staff-registration-page">

      <div className="staff-page-header">

        <div>
          <h1>Staff Account Registration</h1>
          <p>
            Create login credentials for a new agent or administrator.
          </p>
        </div>

        <button
          className="staff-back-btn"
          onClick={() => navigate("/admin/dashboard")}
        >
          <FaArrowLeft />
          Back to Dashboard
        </button>

      </div>

      <div className="staff-registration-layout">

        <div className="staff-info-card">

          <div className="staff-info-icon">
            <FaUserPlus />
          </div>

          <h2>Create Staff Account</h2>

          <p>
            Use this form when assigning a new support agent or administrator.
            The credentials entered here will be used by the staff member to
            sign in to the portal.
          </p>

          <div className="staff-info-list">

            <div className="staff-info-item">
              <FaUserTie />
              <div>
                <strong>Agent</strong>
                <span>
                  Can access assigned complaints and agent features.
                </span>
              </div>
            </div>

            <div className="staff-info-item">
              <FaUserShield />
              <div>
                <strong>Admin</strong>
                <span>
                  Can access the administration dashboard and management
                  features.
                </span>
              </div>
            </div>

          </div>

        </div>

        <div className="staff-form-card">

          <div className="staff-form-header">
            <h2>Account Details</h2>
            <p>Enter the credentials you want to assign.</p>
          </div>

          {error && (
            <div className="staff-message staff-error">
              {error}
            </div>
          )}

          {success && (
            <div className="staff-message staff-success">
              {success}
            </div>
          )}

          <form onSubmit={handleSubmit}>

            <label className="staff-label">
              Account Type
            </label>

            <div className="staff-role-selection">

              <button
                type="button"
                className={
                  role === "agent"
                    ? "staff-role-card selected"
                    : "staff-role-card"
                }
                onClick={() => setRole("agent")}
              >
                <FaUserTie />

                <div>
                  <strong>Agent</strong>
                  <span>Support staff</span>
                </div>
              </button>

              <button
                type="button"
                className={
                  role === "admin"
                    ? "staff-role-card selected"
                    : "staff-role-card"
                }
                onClick={() => setRole("admin")}
              >
                <FaUserShield />

                <div>
                  <strong>Admin</strong>
                  <span>Administrator</span>
                </div>
              </button>

            </div>

            <div className="staff-input-group">

              <label>Full Name</label>

              <div className="staff-input-wrapper">
                <FaUser />

                <input
                  type="text"
                  placeholder="Enter full name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>

            </div>

            <div className="staff-input-group">

              <label>Email Address</label>

              <div className="staff-input-wrapper">
                <FaEnvelope />

                <input
                  type="email"
                  placeholder="Enter email address"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>

            </div>

            <div className="staff-input-group">

              <label>Password</label>

              <div className="staff-input-wrapper">
                <FaLock />

                <input
                  type="password"
                  placeholder="Create password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

            </div>

            <div className="staff-input-group">

              <label>Confirm Password</label>

              <div className="staff-input-wrapper">
                <FaLock />

                <input
                  type="password"
                  placeholder="Confirm password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
              </div>

            </div>

            <button type="submit" className="create-staff-btn">
              <FaUserPlus />
              Create {role === "agent" ? "Agent" : "Admin"} Account
            </button>

          </form>

        </div>

      </div>

    </div>
  );
}

export default StaffRegistration;