import { useEffect, useRef, useState } from "react";
import { FaCamera, FaTimes } from "react-icons/fa";
import {
  getProfilePhoto,
  saveProfilePhoto,
  subscribeToProfilePhotoUpdates,
  validateProfilePhoto,
} from "../../utils/profilePhotos";
import "./ProfilePhotoEditor.css";

function initials(name) {
  return (name || "User")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join("");
}

function ProfileAvatar({ role, name, size = "medium", onClick, className = "" }) {
  const [photo, setPhoto] = useState(() => getProfilePhoto(role));

  useEffect(() => {
    return subscribeToProfilePhotoUpdates(({ role: updatedRole, photo: updatedPhoto }) => {
      if (updatedRole === role) setPhoto(updatedPhoto || "");
    });
  }, [role]);

  return (
    <button
      type="button"
      className={`profile-avatar ${size} ${className}`}
      onClick={onClick}
      aria-label="Change profile photo"
    >
      {photo ? <img src={photo} alt={name} /> : <span>{initials(name)}</span>}
      {onClick && (
        <span className="profile-avatar-camera" aria-hidden="true">
          <FaCamera />
        </span>
      )}
    </button>
  );
}

function ProfilePhotoEditor({ role, name, trigger = "avatar", open, onClose, onOpen }) {
  const [internalOpen, setInternalOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [preview, setPreview] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const inputRef = useRef(null);

  const isControlled = typeof open === "boolean";
  const isOpen = isControlled ? open : internalOpen;

  const close = () => {
    setSelectedFile(null);
    setPreview("");
    setError("");
    if (!isControlled) setInternalOpen(false);
    onClose?.();
  };

  const openEditor = () => {
    setError("");
    if (isControlled) {
      onOpen?.();
      return;
    }
    setInternalOpen(true);
  };

  const handleFileChange = (event) => {
    const file = event.target.files?.[0];
    event.target.value = "";
    const validation = validateProfilePhoto(file);
    if (!validation.ok) {
      setSelectedFile(null);
      setPreview("");
      setError(validation.reason);
      return;
    }

    setError("");
    setSelectedFile(file);
    const reader = new FileReader();
    reader.onload = () => setPreview(String(reader.result || ""));
    reader.onerror = () => {
      setSelectedFile(null);
      setPreview("");
      setError("The selected image could not be read.");
    };
    reader.readAsDataURL(file);
  };

  const handleSave = () => {
    if (!selectedFile || !preview) {
      setError("Please choose an image before saving.");
      return;
    }

    setSaving(true);
    setError("");
    try {
      saveProfilePhoto(role, preview);
      close();
    } catch (err) {
      setError(err.message || "The photo could not be saved.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      {trigger === "avatar" ? (
        <ProfileAvatar role={role} name={name} onClick={openEditor} />
      ) : (
        <button type="button" className="profile-edit-trigger" onClick={openEditor}>
          Change Profile Photo
        </button>
      )}

      {isOpen && (
        <div className="profile-photo-overlay" onClick={close}>
          <div className="profile-photo-modal" onClick={(event) => event.stopPropagation()}>
            <div className="profile-photo-header">
              <div>
                <h3>Change Profile Photo</h3>
                <p>Choose a new photo for your profile.</p>
              </div>
              <button type="button" className="profile-photo-close" onClick={close} aria-label="Close">
                <FaTimes />
              </button>
            </div>

            <div className="profile-photo-body">
              <div className="profile-photo-preview-wrap">
                {preview ? (
                  <img className="profile-photo-preview" src={preview} alt="Selected preview" />
                ) : (
                  <ProfileAvatar role={role} name={name} size="large" />
                )}
              </div>

              <input
                ref={inputRef}
                type="file"
                accept="image/jpeg,image/jpg,image/png,image/webp"
                onChange={handleFileChange}
                hidden
              />

              <button type="button" className="profile-choose-btn" onClick={() => inputRef.current?.click()}>
                <FaCamera /> Choose Image
              </button>
              <p className="profile-photo-help">JPG, JPEG, PNG or WEBP · Maximum 5MB</p>
              {error && <p className="profile-photo-error">{error}</p>}
            </div>

            <div className="profile-photo-footer">
              <button type="button" className="profile-cancel-btn" onClick={close} disabled={saving}>
                Cancel
              </button>
              <button type="button" className="profile-save-btn" onClick={handleSave} disabled={saving}>
                {saving ? "Saving..." : "Save Photo"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default ProfilePhotoEditor;
export { ProfileAvatar };