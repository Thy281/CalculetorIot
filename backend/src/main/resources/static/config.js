// ── Config Module ─────────────────────────────────────────
// Change API_BASE to point to your back-end host/port.
// Example for production (uses same origin):
//   window.API_BASE = "/api/calculations";
// Example for local development:
//   window.API_BASE = "http://localhost:8080/api/calculations";

const apiBase = window.API_BASE || (window.location.protocol + "//" + window.location.host + "/api/calculations");
window.API_BASE = apiBase;

window.SERIAL_BRIDGE_URL = window.SERIAL_BRIDGE_URL || window.location.protocol + "//" + window.location.host;
