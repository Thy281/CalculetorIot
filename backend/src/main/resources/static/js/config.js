// ── Config Module ─────────────────────────────────────────────────────────────
// Change API_BASE to point to your back-end host/port.
// Example for Docker internal:
//   window.API_BASE = "http://calculator-iot-backend:8080/api/calculations";
// Example for Caddy proxy:
//   window.API_BASE = "https://iot.killdev.xyz/api/calculations";

window.API_BASE = window.API_BASE || "https://iot.killdev.xyz/api";

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
