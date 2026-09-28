import { useState } from "react";
import { FaPlus, FaUserCircle, FaCog } from "react-icons/fa";
import { STATUS_LABELS } from "../../utils/mockComplaints";
import "./ComplaintUpdates.css";

function formatDateTime(iso) {
  try {
    return new Date(iso).toLocaleString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
      hour: "numeric",
      minute: "2-digit",
    });
  } catch {
    return iso;
  }
}

function entryLabel(entry) {
  switch (entry.type) {
    case "created":
      return "Complaint Created";
    case "resolution_confirmation":
      return "Resolution Confirmation";
    case "closed":
      return "Complaint Closed";
    case "status_change":
      return "Status Changed";
    case "category_change":
      return "Category Changed";
    default:
      return `${entry.role} Update`;
  }
}

// Chronological Activity/Updates timeline + the "Add Update" form. System
// entries (created/status changes/resolution confirmation) are visually
// distinct from manual User/Agent updates, per spec section 10.
function ComplaintUpdates({ complaint, canAddUpdate, onAddUpdate }) {
  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const sorted = [...(complaint.updates || [])].sort(
    (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
  );

  const handleCancel = () => {
    setShowForm(false);
    setTitle("");
    setDescription("");
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() && !description.trim()) {
      setError("Please provide an update title or description.");
      return;
    }
    setSubmitting(true);
    try {
      await onAddUpdate({ title: title.trim(), description: description.trim() });
      handleCancel();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="updates-section">
      <div className="updates-header">
        <h4>Activity &amp; Updates</h4>
        {canAddUpdate && !showForm && (
          <button type="button" className="add-update-btn" onClick={() => setShowForm(true)}>
            <FaPlus /> Add Update
          </button>
        )}
      </div>

      {showForm && (
        <form className="update-form" onSubmit={handleSubmit}>
          <label>Update Title (Optional)</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Inspection completed"
          />

          <label>Description / Remark (Optional)</label>
          <textarea
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Add details about this update..."
          />

          {error && <p className="update-form-error">{error}</p>}

          <div className="update-form-actions">
            <button type="button" className="cancel-btn" onClick={handleCancel} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="submit-update-btn" disabled={submitting}>
              {submitting ? "Adding..." : "Add Update"}
            </button>
          </div>
        </form>
      )}

      {sorted.length === 0 ? (
        <p className="updates-empty">No activity yet.</p>
      ) : (
        <div className="updates-timeline">
          {sorted.map((entry) => {
            const isSystem = entry.role === "System";
            return (
              <div className={`update-entry ${isSystem ? "system" : "manual"}`} key={entry.id}>
                <div className="update-dot">{isSystem ? <FaCog /> : <FaUserCircle />}</div>
                <div className="update-content">
                  <div className="update-meta">
                    <span className="update-label">{entryLabel(entry)}</span>
                    {!isSystem && <span className="update-role">{entry.role}</span>}
                    <span className="update-date">{formatDateTime(entry.createdAt)}</span>
                  </div>
                  {entry.type === "status_change" && entry.statusFrom && (
                    <p className="update-status-change">
                      {STATUS_LABELS[entry.statusFrom]} → {STATUS_LABELS[entry.statusTo]}
                    </p>
                  )}
                  {entry.title && <p className="update-title">{entry.title}</p>}
                  {entry.description && <p className="update-description">{entry.description}</p>}
                  {!isSystem && entry.createdBy && <p className="update-author">— {entry.createdBy}</p>}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default ComplaintUpdates;