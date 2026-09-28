import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import AdminLayout from "./components/layout/AdminLayout";
import PortalLayout from "./components/layout/PortalLayout";

import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";

import Dashboard from "./pages/admin/Dashboard";
import Complaints from "./pages/admin/Complaints";
import UserManagement from "./pages/admin/UserManagement";
import Agents from "./pages/admin/Agents";
import Analytics from "./pages/admin/Analytics";
import Reports from "./pages/admin/Reports";
import Notifications from "./pages/admin/Notifications";
import StaffRegistration from "./pages/admin/StaffRegistration";

import RaiseComplaint from "./pages/customer/RaiseComplaint";
import MyComplaints from "./pages/customer/MyComplaints";
import ComplaintDetails from "./pages/customer/ComplaintDetails";

import AgentDashboard from "./pages/agent/Dashboard";
import AgentComplaintDetail from "./pages/agent/ComplaintDetail";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* Default */}
        <Route
          path="/"
          element={<Navigate to="/login" replace />}
        />

        {/* Authentication */}
        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

        {/* Admin */}
        <Route
          path="/admin"
          element={<AdminLayout />}
        >
          <Route
            path="dashboard"
            element={<Dashboard />}
          />

          <Route
            path="complaints"
            element={<Complaints />}
          />

          <Route
            path="users"
            element={<UserManagement />}
          />

          <Route
            path="agents"
            element={<Agents />}
          />

          <Route
            path="staff-registration"
            element={<StaffRegistration />}
          />

          <Route
            path="analytics"
            element={<Analytics />}
          />

          <Route
            path="reports"
            element={<Reports />}
          />

          <Route
            path="notifications"
            element={<Notifications />}
          />
        </Route>

        {/* Customer / User */}
        <Route
          path="/customer"
          element={<PortalLayout role="customer" />}
        >
          <Route
            index
            element={<Navigate to="complaints" replace />}
          />

          <Route
            path="raise"
            element={<RaiseComplaint />}
          />

          <Route
            path="complaints"
            element={<MyComplaints />}
          />

          <Route
            path="complaints/:id"
            element={<ComplaintDetails />}
          />
        </Route>

        {/* Agent */}
        <Route
          path="/agent"
          element={<PortalLayout role="agent" />}
        >
          <Route
            path="dashboard"
            element={<AgentDashboard />}
          />

          <Route
            path="complaints/:id"
            element={<AgentComplaintDetail />}
          />
        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;