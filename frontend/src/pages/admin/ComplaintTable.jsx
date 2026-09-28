import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./ComplaintTable.css";
import ComplaintDetailsModal from "./ComplaintDetailsModal";
import { getComplaints, STATUS, STATUS_LABELS } from "../../utils/mockComplaints";

const STATUS_CLASS = {
  [STATUS.OPEN]: "pending",
  [STATUS.ASSIGNED]: "progress",
  [STATUS.IN_PROGRESS]: "progress",
  [STATUS.RESOLVED]: "resolved",
  [STATUS.CLOSED]: "resolved",
  [STATUS.REJECTED]: "pending",
  [STATUS.NEEDS_REASSIGNMENT]: "progress",
};

function ComplaintTable() {
  const navigate = useNavigate();
  const [complaints, setComplaints] = useState([]);
  const [selectedId, setSelectedId] = useState(null);

  const refresh = () => {
    const all = getComplaints();
    const recent = [...all]
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      .slice(0, 5);
    setComplaints(recent);
  };

  useEffect(() => {
    refresh();
  }, []);

  const getPriorityClass = (priority) => {
    switch (priority) {
      case "High":
        return "high";
      case "Medium":
        return "medium";
      case "Low":
        return "low";
      default:
        return "";
    }
  };

  const renderAgentCell = (assignedAgent, status) => {
    if (!assignedAgent) {
      return (
        <span className={`agent-status ${status === STATUS.NEEDS_REASSIGNMENT ? "rejected" : "unassigned"}`}>
          {status === STATUS.NEEDS_REASSIGNMENT ? "Needs Reassignment" : "Unassigned"}
        </span>
      );
    }

    const waiting = status === STATUS.ASSIGNED;
    return (
      <div className="agent-cell">
        <span className="agent-name">{assignedAgent}</span>
        <span className={`agent-status ${waiting ? "pending-response" : "accepted"}`}>
          {waiting ? "Waiting for Acceptance" : "Assigned"}
        </span>
      </div>
    );
  };

  return (
    <div className="table-card">
      <div className="table-header">
        <div>
          <h2>Recent Complaints</h2>
          <p>Latest complaints received</p>
        </div>

        <button
          className="view-all-btn"
          onClick={() => navigate("/admin/complaints")}
        >
          View All
        </button>
      </div>

      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Citizen</th>
              <th>Category</th>
              <th>Priority</th>
              <th>Status</th>
              <th>Agent</th>
              <th></th>
            </tr>
          </thead>

          <tbody>
            {complaints.map((item) => (
              <tr key={item.id}>
                <td className="complaint-id">{item.id}</td>
                <td>{item.createdBy}</td>
                <td>{item.category}</td>

                <td>
                  <span className={`priority ${getPriorityClass(item.priority)}`}>
                    {item.priority}
                  </span>
                </td>

                <td>
                  <span className={`status ${STATUS_CLASS[item.status] || ""}`}>
                    {STATUS_LABELS[item.status] || item.status}
                  </span>
                </td>

                <td>{renderAgentCell(item.assignedAgent, item.status)}</td>

                <td>
                  <button
                    className="table-action"
                    onClick={() => setSelectedId(item.id)}
                  >
                    View
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ComplaintDetailsModal
        complaintId={selectedId}
        onClose={() => setSelectedId(null)}
        onChanged={refresh}
      />
    </div>
  );
}

export default ComplaintTable;

