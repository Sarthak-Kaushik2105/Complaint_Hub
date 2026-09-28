import "./Sidebar.css";
import ProfilePhotoEditor from "../common/ProfilePhotoEditor";

import { useNavigate, useLocation } from "react-router-dom";

import {
  FaHome,
  FaUsers,
  FaUserTie,
  FaClipboardList,
  FaChartPie,
  FaFileAlt,
  FaBell,
  FaSignOutAlt,
  FaPlusCircle,
  FaUserPlus,
} from "react-icons/fa";

function Sidebar({ role = "admin" }) {
  const navigate = useNavigate();
  const location = useLocation();

  const menus = {
    admin: [
      {
        icon: <FaHome />,
        text: "Dashboard",
        path: "/admin/dashboard",
      },
      {
        icon: <FaUsers />,
        text: "Users",
        path: "/admin/users",
      },
      {
        icon: <FaUserTie />,
        text: "Agents",
        path: "/admin/agents",
      },
      {
        icon: <FaUserPlus />,
        text: "Staff Accounts",
        path: "/admin/staff-registration",
      },
      {
        icon: <FaClipboardList />,
        text: "Complaints",
        path: "/admin/complaints",
      },
      {
        icon: <FaChartPie />,
        text: "Analytics",
        path: "/admin/analytics",
      },
      {
        icon: <FaFileAlt />,
        text: "Reports",
        path: "/admin/reports",
      },
      {
        icon: <FaBell />,
        text: "Notifications",
        path: "/admin/notifications",
      },
    ],

    customer: [
      {
        icon: <FaPlusCircle />,
        text: "Raise Complaint",
        path: "/customer/raise",
      },
      {
        icon: <FaClipboardList />,
        text: "My Complaints",
        path: "/customer/complaints",
      },
    ],

    agent: [
      {
        icon: <FaClipboardList />,
        text: "Assigned Complaints",
        path: "/agent/dashboard",
      },
    ],
  };

  const profileLabels = {
    admin: {
      name: "Administrator",
      tag: "Admin",
    },

    customer: {
      name: "Customer",
      tag: "User",
    },

    agent: {
      name: "Support Agent",
      tag: "Agent",
    },
  };

  const profile = profileLabels[role] || profileLabels.admin;

  const handleLogout = () => {
    localStorage.removeItem("role");
    localStorage.removeItem("currentUser");
    navigate("/login");
  };

  return (
    <aside className="sidebar">

      <div className="sidebar-main">

        <div className="sidebar-logo">

          <div className="logo-box">
            C
          </div>

          <div>
            <h2>ComplaintHub</h2>
            <p>Service Portal</p>
          </div>

        </div>

        <div className="profile-box">

          <ProfilePhotoEditor
            role={role}
            name={profile.name}
          />

          <h3>{profile.name}</h3>

          <span>
            {profile.tag}
          </span>

        </div>

        <nav>

          {(menus[role] || menus.admin).map((item) => (
            <button
              key={item.text}
              className={
                location.pathname === item.path
                  ? "menu-item active"
                  : "menu-item"
              }
              onClick={() => navigate(item.path)}
            >

              <span>
                {item.icon}
              </span>

              <p>
                {item.text}
              </p>

            </button>
          ))}

        </nav>

      </div>

      <div className="sidebar-footer">

        <button
          className="logout-btn"
          onClick={handleLogout}
        >
          <FaSignOutAlt />

          <span>
            Logout
          </span>
        </button>

      </div>

    </aside>
  );
}

export default Sidebar;