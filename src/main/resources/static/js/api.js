/**
 * Collecteo - couche d'accès à l'API REST existante (/api/**).
 * L'authentification est un JWT stateless (voir SecurityConfig / JwtAuthFilter côté backend) :
 * on le stocke côté navigateur et on le renvoie dans l'en-tête Authorization sur chaque appel.
 */
const API_BASE = "/api";
const TOKEN_KEY = "collecteo_token";
const USER_KEY = "collecteo_user";

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY));
  } catch (e) {
    return null;
  }
}

function setSession(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

function logout() {
  clearSession();
  window.location.href = "/login";
}

/**
 * A appeler en haut de chaque page protégée : redirige vers /login si pas de token,
 * affiche l'utilisateur courant dans la barre latérale, masque les liens réservés à l'admin.
 */
function requireAuth() {
  if (!getToken()) {
    window.location.href = "/login";
    return;
  }
  const user = getUser();
  const el = document.getElementById("nomUtilisateurCourant");
  if (el && user) {
    el.textContent = user.nom + " (" + user.role + ")";
  }
  if (user && user.role !== "ADMINISTRATEUR") {
    document.querySelectorAll(".admin-only").forEach((e) => (e.style.display = "none"));
  }
}

/** Appel générique à l'API JSON. Lève une Error avec un message lisible en cas d'échec. */
async function api(path, options = {}) {
  const headers = Object.assign({ "Content-Type": "application/json" }, options.headers || {});
  const token = getToken();
  if (token) headers["Authorization"] = "Bearer " + token;

  const resp = await fetch(API_BASE + path, Object.assign({}, options, { headers }));

  if (resp.status === 401) {
    clearSession();
    window.location.href = "/login";
    throw new Error("Session expirée, merci de vous reconnecter.");
  }

  if (!resp.ok) {
    let msg = "Erreur " + resp.status;
    try {
      const data = await resp.json();
      msg = data.message || data.error || msg;
    } catch (e) {
      /* pas de corps JSON */
    }
    throw new Error(msg);
  }

  if (resp.status === 204) return null;
  const contentType = resp.headers.get("content-type") || "";
  if (contentType.includes("application/json")) return resp.json();
  return null;
}

/** Télécharge un fichier binaire (export Excel/PDF) protégé par JWT. */
async function apiDownload(path, filename) {
  const token = getToken();
  const resp = await fetch(API_BASE + path, { headers: { Authorization: "Bearer " + token } });
  if (!resp.ok) {
    alert("Le téléchargement a échoué (" + resp.status + ").");
    return;
  }
  const blob = await resp.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

function formatMontant(v) {
  const n = Number(v || 0);
  return n.toLocaleString("fr-FR", { maximumFractionDigits: 2 }) + " Ar";
}

function formatDate(v) {
  if (!v) return "—";
  return new Date(v).toLocaleString("fr-FR");
}

function formatDateSeule(v) {
  if (!v) return "—";
  return new Date(v).toLocaleDateString("fr-FR");
}

const LIBELLES_STATUT = {
  PAYEE: "Payée",
  PARTIELLEMENT_PAYEE: "Partiellement payée",
  IMPAYEE: "Impayée",
  EN_COURS: "En cours",
  SOLDE: "Soldé",
  EN_RETARD: "En retard",
};

function badgeStatut(s) {
  const couleurs = {
    PAYEE: "success",
    PARTIELLEMENT_PAYEE: "warning",
    IMPAYEE: "danger",
    EN_COURS: "primary",
    SOLDE: "success",
    EN_RETARD: "danger",
  };
  const c = couleurs[s] || "secondary";
  const libelle = LIBELLES_STATUT[s] || s;
  return '<span class="badge text-bg-' + c + '">' + libelle + "</span>";
}

function showError(msg) {
  const box = document.getElementById("errorBox");
  if (box) {
    box.textContent = msg;
    box.classList.remove("d-none");
  } else {
    alert(msg);
  }
}

function clearError() {
  const box = document.getElementById("errorBox");
  if (box) {
    box.classList.add("d-none");
    box.textContent = "";
  }
}

function showSuccess(msg) {
  const box = document.getElementById("successBox");
  if (box) {
    box.textContent = msg;
    box.classList.remove("d-none");
    setTimeout(() => box.classList.add("d-none"), 3000);
  }
}

function escapeHtml(v) {
  if (v === null || v === undefined) return "";
  return String(v)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}
