import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./AdminTopbar.css";

import {
  FaBell,
  FaSearch,
  FaCalendarAlt,
  FaChevronDown,
  FaSignOutAlt,
  FaUser,
} from "react-icons/fa";
import AdminProfileModal from "./AdminProfileModal";
import { ProfileAvatar } from "../../components/common/ProfilePhotoEditor";

function AdminTopbar() {
  const navigate = useNavigate();
  const [profileOpen, setProfileOpen] = useState(false);
  const [profileModalOpen, setProfileModalOpen] = useState(false);

  const today = new Date().toLocaleDateString("en-US", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });

  const handleLogout = () => {
    localStorage.removeItem("role");
    navigate("/login");
  };

  return (
    <header className="admin-topbar">
      <div className="topbar-left">
        <h1>Dashboard</h1>
        <p>Welcome back! Here's what's happening today.</p>
      </div>

      <div className="topbar-right">
        <div className="search-box">
          <FaSearch className="search-icon" />
          <input type="text" placeholder="Search complaints, users..." />
        </div>

        <div className="topbar-date">
          <FaCalendarAlt />
          <span>{today}</span>
        </div>

        <button
          className="notification-btn"
          onClick={() => navigate("/admin/notifications")}
          aria-label="Notifications"
        >
          <FaBell />
          <span className="notification-badge">4</span>
        </button>

        <div className="profile-wrapper">
          <div
            className="admin-profile"
            role="button"
            tabIndex={0}
            onClick={() => setProfileOpen((prev) => !prev)}
            onKeyDown={(event) => {
              if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                setProfileOpen((prev) => !prev);
              }
            }}
            aria-label="Open admin profile menu"
          >
            <ProfileAvatar role="admin" name="Administrator" size="small" />
            <div className="profile-info">
              <h4>Administrator</h4>
              <p>Super Admin</p>
            </div>
            <FaChevronDown className="profile-arrow" />
          </div>

          {profileOpen && (
            <div className="profile-dropdown">
              <button
                className="dropdown-item"
                onClick={() => {
                  setProfileModalOpen(true);
                  setProfileOpen(false);
                }}
              >
                <FaUser /> Profile
              </button>
              <button className="dropdown-item logout" onClick={handleLogout}>
                <FaSignOutAlt /> Logout
              </button>
            </div>
          )}
        </div>
      </div>

      <AdminProfileModal open={profileModalOpen} onClose={() => setProfileModalOpen(false)} />
    </header>
  );
}

export default AdminTopbar;
