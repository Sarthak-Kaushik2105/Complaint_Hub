import { useState } from "react";
import { FaTimes } from "react-icons/fa";
import ProfilePhotoEditor, { ProfileAvatar } from "../../components/common/ProfilePhotoEditor";
import "../../components/common/Modal.css";

const ADMIN_NAME = "Administrator";

function AdminProfileModal({ open, onClose }) {
  const [photoEditorOpen, setPhotoEditorOpen] = useState(false);

  if (!open) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={(event) => event.stopPropagation()}>
        <div className="modal-header">
          <h2>Your Profile</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close profile">
            <FaTimes />
          </button>
        </div>

        <div className="modal-body admin-profile-modal-body">
          <div className="admin-profile-photo-area">
            <ProfileAvatar role="admin" name={ADMIN_NAME} size="large" />
            <ProfilePhotoEditor
              role="admin"
              name={ADMIN_NAME}
              trigger="button"
              open={photoEditorOpen}
              onOpen={() => setPhotoEditorOpen(true)}
              onClose={() => setPhotoEditorOpen(false)}
            />
          </div>

          <div className="modal-row">
            <span className="modal-label">Name</span>
            <span className="modal-value">Administrator</span>
          </div>

          <div className="modal-row">
            <span className="modal-label">Email</span>
            <span className="modal-value">admin@complainthub.com</span>
          </div>

          <div className="modal-row">
            <span className="modal-label">Role</span>
            <span className="modal-value">Super Admin</span>
          </div>

          <div className="modal-row">
            <span className="modal-label">Department</span>
            <span className="modal-value">Platform Administration</span>
          </div>
        </div>

        <div className="modal-footer">
          <button className="modal-secondary-btn" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

export default AdminProfileModal;



