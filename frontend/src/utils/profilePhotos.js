const PROFILE_PHOTO_PREFIX = "complainthub:profile-photo:";
const PROFILE_PHOTO_EVENT = "complainthub:profile-photo-updated";

export const PROFILE_PHOTO_MAX_SIZE_MB = 5;
export const PROFILE_PHOTO_TYPES = ["image/jpeg", "image/jpg", "image/png", "image/webp"];

function keyFor(role) {
  return `${PROFILE_PHOTO_PREFIX}${role}`;
}

export function getProfilePhoto(role) {
  if (!role) return "";
  try {
    return localStorage.getItem(keyFor(role)) || "";
  } catch {
    return "";
  }
}

export function saveProfilePhoto(role, dataUrl) {
  if (!role || !dataUrl) {
    throw new Error("A valid profile photo is required.");
  }

  try {
    localStorage.setItem(keyFor(role), dataUrl);
    window.dispatchEvent(
      new CustomEvent(PROFILE_PHOTO_EVENT, {
        detail: { role, photo: dataUrl },
      })
    );
  } catch {
    throw new Error("The photo could not be saved. Try a smaller image.");
  }
}

export function subscribeToProfilePhotoUpdates(callback) {
  const handler = (event) => callback(event.detail || {});
  window.addEventListener(PROFILE_PHOTO_EVENT, handler);
  return () => window.removeEventListener(PROFILE_PHOTO_EVENT, handler);
}

export function validateProfilePhoto(file) {
  if (!file) return { ok: false, reason: "Please choose an image." };
  if (!PROFILE_PHOTO_TYPES.includes(file.type)) {
    return { ok: false, reason: "Use a JPG, JPEG, PNG, or WEBP image." };
  }
  if (file.size > PROFILE_PHOTO_MAX_SIZE_MB * 1024 * 1024) {
    return { ok: false, reason: `Profile photo must be ${PROFILE_PHOTO_MAX_SIZE_MB}MB or smaller.` };
  }
  return { ok: true };
  }

export { PROFILE_PHOTO_EVENT };