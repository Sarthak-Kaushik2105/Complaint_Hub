import { useEffect, useState } from "react";
import {
  FaUserTie,
  FaCheckCircle,
  FaExclamationTriangle,
  FaBan,
  FaCheck,
  FaTimes,
} from "react-icons/fa";
import {
  getComplaintById,
  addUpdate,
  acceptComplaint,
  resolveComplaint,
  confirmResolution,
  assignAgent,
  rejectComplaint,
  updateCategory,
  updatePriority,
  STATUS,
  STATUS_LABELS,
  PRIORITIES,
  CATEGORIES,
  AVAILABLE_AGENTS,
} from "../../utils/mockComplaints";
import ComplaintOriginal from "./ComplaintOriginal";
import ComplaintLifecycle from "./ComplaintLifecycle";
import ResolutionConfirmation from "./ResolutionConfirmation";
import ComplaintUpdates from "./ComplaintUpdates";
import "./ComplaintDetailPanel.css";
import "./ComplaintActionControls.css";

const STATUS_BADGE_CLASS = {
  [STATUS.OPEN]: "badge-open",
  [STATUS.ASSIGNED]: "badge-assigned",
  [STATUS.IN_PROGRESS]: "badge-progress",
  [STATUS.RESOLVED]: "badge-resolved",
  [STATUS.CLOSED]: "badge-closed",
  [STATUS.REJECTED]: "badge-rejected",
  [STATUS.NEEDS_REASSIGNMENT]: "badge-reassignment",
};

function ComplaintDetailPanel({
  complaintId,
  role,
  currentUserName = "You",
  variant = "page",
  onChanged,
  onNotFound,
}) {
  const [complaint, setComplaint] = useState(undefined);
  const [resolveNote, setResolveNote] = useState("");
  const [rejectReason, setRejectReason] = useState("");
  const [showResolveForm, setShowResolveForm] = useState(false);
  const [showRejectForm, setShowRejectForm] = useState(false);
  const [showAssignList, setShowAssignList] = useState(false);
  const [categoryDraft, setCategoryDraft] = useState("");
  const [actionError, setActionError] = useState("");

  const refresh = () => {
    const found = getComplaintById(complaintId);
    setComplaint(found);
    if (found) setCategoryDraft(found.category);
    if (!found) onNotFound?.();
    return found;
  };

  useEffect(() => {
    // The mock service is synchronous; this effect keeps the panel in sync when the route changes.
    // eslint-disable-next-line react-hooks/set-state-in-effect, react-hooks/exhaustive-deps
    refresh();
  }, [complaintId]);

  if (complaint === undefined || complaint === null) return null;

  const notify = () => {
    refresh();
    onChanged?.();
  };

  const runAction = (action) => {
    setActionError("");
    try {
      action();
      notify();
    } catch (err) {
      setActionError(err.message || "The action could not be completed.");
    }
  };

  const handleAddUpdate = async ({ title, description }) => {
    const authorRole = role === "customer" ? "User" : role === "agent" ? "Agent" : "Admin";
    addUpdate(complaint.id, {
      title,
      description,
      author: currentUserName,
      role: authorRole,
    });
    notify();
  };

  const handleAccept = () => {
    runAction(() => acceptComplaint(complaint.id, currentUserName));
  };

  const handleReject = (event) => {
    event.preventDefault();
    runAction(() =>
      rejectComplaint(complaint.id, {
        reason: rejectReason,
        author: currentUserName,
        actorRole: "Agent",
      })
    );
    setRejectReason("");
    setShowRejectForm(false);
  };

  const handleConfirmResolution = (solved) => {
    runAction(() => confirmResolution(complaint.id, solved));
  };

  const handleResolve = (event) => {
    event.preventDefault();
    runAction(() =>
      resolveComplaint(complaint.id, {
        description: resolveNote.trim(),
        author: currentUserName,
      })
    );
    setResolveNote("");
    setShowResolveForm(false);
  };

  const handleAssign = (agentName) => {
    runAction(() => assignAgent(complaint.id, agentName));
    setShowAssignList(false);
  };

  const handleCategorySave = () => {
    runAction(() => updateCategory(complaint.id, categoryDraft, { author: currentUserName }));
  };

  const handlePriorityChange = (event) => {
    runAction(() => updatePriority(complaint.id, event.target.value));
  };

  const canAddUpdate =
    role === "customer"
      ? ![STATUS.CLOSED, STATUS.REJECTED].includes(complaint.status)
      : role === "agent"
      ? complaint.status === STATUS.IN_PROGRESS && complaint.assignedAgent === currentUserName
      : false;

  const agentAwaitingDecision =
    role === "agent" &&
    complaint.status === STATUS.ASSIGNED &&
    complaint.assignedAgent === currentUserName;
  const agentCanResolve =
    role === "agent" &&
    complaint.status === STATUS.IN_PROGRESS &&
    complaint.assignedAgent === currentUserName;
  const customerConfirms = role === "customer" && complaint.status === STATUS.RESOLVED;
  const adminCanAssign =
    role === "admin" && [STATUS.OPEN, STATUS.NEEDS_REASSIGNMENT].includes(complaint.status);
  const adminCanReject = role === "admin" && complaint.status === STATUS.OPEN;
  const adminNeedsAttention = role === "admin" && complaint.status === STATUS.NEEDS_REASSIGNMENT;

  return (
    <div className={variant === "embedded" ? "panel embedded" : "panel details-card"}>
      <div className="panel-header">
        <div>
          <h1 className="panel-title">{complaint.title}</h1>
          <p className="panel-id">Complaint #{complaint.id}</p>
        </div>
        <span className={`status-badge ${STATUS_BADGE_CLASS[complaint.status]}`}>
          {STATUS_LABELS[complaint.status]}
        </span>
      </div>

      {actionError && (
        <div className="panel-action-error" role="alert">
          {actionError}
          <button type="button" onClick={() => setActionError("")} aria-label="Dismiss error">
            <FaTimes />
          </button>
        </div>
      )}

      <ComplaintOriginal complaint={complaint} />

      {role === "admin" && (
        <div className="panel-section category-editor">
          <div className="editor-heading">
            <div>
              <h4>Verified Category</h4>
              <p>Customer selected a category when the complaint was raised. Admin has final authority.</p>
            </div>
          </div>
          <div className="category-editor-row">
            <select value={categoryDraft} onChange={(event) => setCategoryDraft(event.target.value)}>
              {CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
            <button type="button" className="secondary-action-btn" onClick={handleCategorySave}>
              Save Category
            </button>
          </div>
        </div>
      )}

      {role === "admin" && (
        <div className="panel-section priority-editor">
          <h4>Priority</h4>
          <select value={complaint.priority} onChange={handlePriorityChange}>
            {PRIORITIES.map((priority) => (
              <option key={priority} value={priority}>
                {priority}
              </option>
            ))}
          </select>
        </div>
      )}

      <div className="panel-section">
        <h4>Complaint Lifecycle</h4>
        <ComplaintLifecycle status={complaint.status} />
      </div>

      <div className="panel-section">
        <h4>Assigned Agent</h4>
        <div className="agent-row">
          <FaUserTie />
          <span>{complaint.assignedAgent || "Not assigned — awaiting admin action"}</span>
        </div>
      </div>

      {agentAwaitingDecision && (
        <div className="panel-section agent-decision-section">
          <div className="agent-decision-header">
            <div>
              <h4>Agent Decision Required</h4>
              <p>This complaint has been assigned to you. Accept it to start work, or reject it with a reason.</p>
            </div>
          </div>

          {!showRejectForm ? (
            <div className="agent-decision-actions">
              <button type="button" className="accept-btn" onClick={handleAccept}>
                <FaCheck /> Accept Complaint
              </button>
              <button type="button" className="reject-btn" onClick={() => setShowRejectForm(true)}>
                <FaBan /> Reject Complaint
              </button>
            </div>
          ) : (
            <form className="reject-form" onSubmit={handleReject}>
              <label htmlFor={`reject-reason-${complaint.id}`}>Rejection Reason</label>
              <textarea
                id={`reject-reason-${complaint.id}`}
                rows={3}
                value={rejectReason}
                onChange={(event) => setRejectReason(event.target.value)}
                placeholder="e.g. Wrong department or outside my assigned area"
                required
              />
              <div className="resolve-form-actions">
                <button
                  type="button"
                  className="cancel-btn"
                  onClick={() => {
                    setShowRejectForm(false);
                    setRejectReason("");
                  }}
                >
                  Cancel
                </button>
                <button type="submit" className="reject-confirm-btn">
                  <FaBan /> Confirm Rejection
                </button>
              </div>
            </form>
          )}
        </div>
      )}

      {agentCanResolve && (
        <div className="panel-section">
          {!showResolveForm ? (
            <button type="button" className="primary-action-btn" onClick={() => setShowResolveForm(true)}>
              <FaCheckCircle /> Mark as Resolved
            </button>
          ) : (
            <form className="resolve-form" onSubmit={handleResolve}>
              <label htmlFor={`resolve-note-${complaint.id}`}>Resolution Note (Optional)</label>
              <textarea
                id={`resolve-note-${complaint.id}`}
                rows={3}
                value={resolveNote}
                onChange={(event) => setResolveNote(event.target.value)}
                placeholder="What was done to resolve this?"
              />
              <div className="resolve-form-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowResolveForm(false)}>
                  Cancel
                </button>
                <button type="submit" className="primary-action-btn">
                  <FaCheckCircle /> Confirm Resolved
                </button>
              </div>
            </form>
          )}
        </div>
      )}

      {customerConfirms && (
        <div className="panel-section">
          <ResolutionConfirmation onConfirm={handleConfirmResolution} />
        </div>
      )}

      {adminCanAssign && (
        <div className="panel-section">
          <div className="admin-assign-header">
            <FaExclamationTriangle />
            <span>
              {adminNeedsAttention
                ? "Customer/agent action requires your review. Assign this complaint to an agent."
                : "This complaint needs an agent assigned."}
            </span>
          </div>

          {!showAssignList ? (
            <div className="admin-assign-actions">
              <button type="button" className="primary-action-btn" onClick={() => setShowAssignList(true)}>
                {adminNeedsAttention ? "Reassign Agent" : "Assign Agent"}
              </button>
              {adminCanReject && (
                <button
                  type="button"
                  className="reject-btn"
                  onClick={() => {
                    setActionError("");
                    const reason = window.prompt("Optional rejection reason:", "");
                    if (reason !== null) {
                      runAction(() => rejectComplaint(complaint.id, { reason, author: currentUserName }));
                    }
                  }}
                >
                  <FaBan /> Reject Complaint
                </button>
              )}
            </div>
          ) : (
            <div className="assign-list">
              {AVAILABLE_AGENTS.map((agent) => (
                <button
                  key={agent.name}
                  type="button"
                  className="assign-option"
                  onClick={() => handleAssign(agent.name)}
                >
                  {agent.name}
                  <span>{agent.department}</span>
                </button>
              ))}
              <button type="button" className="cancel-btn" onClick={() => setShowAssignList(false)}>
                Cancel
              </button>
            </div>
          )}
        </div>
      )}

      <div className="panel-section">
        <ComplaintUpdates
          complaint={complaint}
          canAddUpdate={canAddUpdate}
          currentRole={role}
          currentAuthor={currentUserName}
          onAddUpdate={handleAddUpdate}
        />
      </div>
    </div>
  );
}

export default ComplaintDetailPanel;
