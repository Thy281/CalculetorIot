// ── Config Module ─────────────────────────────────────────────────────────────
// Change API_BASE to point to your back-end host/port.
// Example for Spring Boot running locally on 8080:
//   window.API_BASE = "http://127.0.0.1:8080/api/calculations";
// Example for LAN host:
//   window.API_BASE = "http://192.168.0.22:8080/api/calculations";
// Example for Railway:
//   window.API_BASE = "https://calculetoriot.up.railway.app/api/calculations";

window.API_BASE = window.API_BASE || "https://calculetoriot.up.railway.app/api/calculations";

// Optional: HTTP(S) serial bridge for browsers/devices without Web Serial support.
// If your frontend runs on HTTPS, the bridge must also be HTTPS:
//   window.SERIAL_BRIDGE_URL = "https://calculetoriot.up.railway.app/serial";
// Leave empty to disable bridge mode.
window.SERIAL_BRIDGE_URL = window.SERIAL_BRIDGE_URL || "https://calculetoriot.up.railway.app/serial";

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
