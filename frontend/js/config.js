// ── Config Module ─────────────────────────────────────────────────────────────
// Docker: uses nginx proxy (/api/ → backend:8080)
// Local dev: use explicit URL
window.API_BASE = window.API_BASE || "/api";

// Cache busting - force reload on update
// v20260322-01

// Optional: HTTP(S) serial bridge for browsers/devices without Web Serial support.
// If your frontend runs on HTTPS, the bridge must also be HTTPS:
window.SERIAL_BRIDGE_URL = window.SERIAL_BRIDGE_URL || "https://iot.killdev.xyz/serial";

// Optional: USB filters for Web Serial (desktop).
// Leave commented to use the default filters (Arduino, CH340, CP210x, FTDI).
// If your board does not appear in the chooser, define your VID/PID here:
// window.SERIAL_USB_FILTERS = [
//   { usbVendorId: 0x2341 },
//   { usbVendorId: 0x2A03 },
//   { usbVendorId: 0x1A86 }
// ];

// Bluetooth note:
// HC-05/HC-06 use classic Bluetooth (SPP) and are not accessible via Web Bluetooth in browsers.
// To use HC-05/HC-06 with this frontend, use a bridge (SERIAL_BRIDGE_URL) or a native mobile app.
