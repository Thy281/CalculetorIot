// ── Serial Module ─────────────────────────────────────────
// Web Serial API: connect to Arduino and send data.

const SERIAL_BAUD_RATE = 9600;
const SERIAL_BRIDGE_URL = (window.SERIAL_BRIDGE_URL || "").replace(/\/$/, "");
const SERIAL_BRIDGE_CANDIDATES = buildBridgeCandidates(SERIAL_BRIDGE_URL);
const SERIAL_USB_FILTERS = Array.isArray(window.SERIAL_USB_FILTERS)
    ? window.SERIAL_USB_FILTERS
    : [
        { usbVendorId: 0x2341 }, // Arduino SA
        { usbVendorId: 0x2A03 }, // Arduino LLC
        { usbVendorId: 0x1A86 }, // CH340 clones
        { usbVendorId: 0x10C4 }, // CP210x
        { usbVendorId: 0x0403 }  // FTDI
    ];
let serialPort = null;
let serialWriter = null;
let _bridgeConnected = false;
let _lastToneTs = 0;
let _serialMode = "none"; // none | webserial | bridge
let _activeBridgeBase = "";
const SERIAL_TONE_COOLDOWN_MS = 120;

function isLikelyMobileDevice() {
    const ua = navigator.userAgent || "";
    return /Android|iPhone|iPad|iPod|Mobile/i.test(ua);
}

function getUnsupportedSerialMessage() {
    if (window.isSecureContext === false) {
        return "Open the app over HTTPS (or localhost). Web Serial requires a secure context.";
    }

    if (isLikelyMobileDevice()) {
        return "On phones/tablets, browsers often do not expose Web Serial over OTG. HC-05/HC-06 (classic Bluetooth SPP) also do not work in browsers. Use a native Bluetooth serial app or SERIAL_BRIDGE_URL.";
    }

    return "This browser does not support direct USB serial. HC-05/HC-06 (classic Bluetooth SPP) are not supported by Web Bluetooth. Use Chrome/Edge desktop with USB cable or SERIAL_BRIDGE_URL.";
}

function isSerialSupported() {
    return ("serial" in navigator && typeof navigator.serial.requestPort === "function") || !!SERIAL_BRIDGE_URL;
}

function isSerialConnected() {
    return !!serialPort || _bridgeConnected;
}

function updateSerialIndicator(connected) {
    const dot = document.getElementById("serial-dot");
    const label = document.getElementById("serial-label");
    const btn = document.getElementById("btn-serial");
    const btnDisconnect = document.getElementById("btn-serial-disconnect");

    if (dot) {
        dot.className = connected
            ? "w-2 h-2 rounded-full bg-emerald-500 shadow-[0_0_0_2px_rgba(16,185,129,0.3)]"
            : "w-2 h-2 rounded-full bg-gray-600";
    }

    if (label) {
        label.textContent = connected ? "Serial connected" : "Serial disconnected";
        label.className = connected ? "text-xs text-emerald-300" : "text-xs text-gray-500";
    }

    if (btn) {
        btn.textContent = connected ? "Connected" : "Connect";
        btn.disabled = false;
        btn.classList.toggle("bg-emerald-600/80", connected);
        btn.classList.toggle("border-emerald-400/60", connected);
        btn.classList.toggle("text-emerald-100", connected);
        btn.classList.toggle("opacity-60", connected);
        btn.classList.toggle("cursor-not-allowed", connected);
    }

    if (btnDisconnect) {
        if (connected) {
            btnDisconnect.classList.remove("hidden");
            btnDisconnect.disabled = false;
        } else {
            btnDisconnect.classList.add("hidden");
            btnDisconnect.disabled = true;
        }
    }
}

async function connectSerial() {
    const support = {
        webSerial: "serial" in navigator && typeof navigator.serial.requestPort === "function",
        bridge: !!SERIAL_BRIDGE_URL
    };

    if (!isSerialSupported()) {
        const msg = support.bridge
            ? "Failed to detect runtime support (check HTTPS/secure context)."
            : getUnsupportedSerialMessage();
        showToast(msg + ` Bridge: ${SERIAL_BRIDGE_URL || "(empty)"}`, "error");
        console.warn("Serial support check", support, { SERIAL_BRIDGE_URL });
        return;
    }

    if (serialPort || _bridgeConnected) {
        showToast("Serial already connected.", "success");
        return;
    }

    const btn = document.getElementById("btn-serial");
    if (btn) {
        btn.disabled = true;
        btn.textContent = "Connecting...";
    }

    try {
        if (support.webSerial) {
            let port = null;

            // Reuse an already-authorized port when available to avoid chooser issues.
            if (navigator.serial && typeof navigator.serial.getPorts === "function") {
                const ports = await navigator.serial.getPorts();
                if (ports.length > 0) {
                    port = ports[0];
                }
            }

            if (!port) {
                port = await navigator.serial.requestPort({ filters: SERIAL_USB_FILTERS });
            }

            await port.open({ baudRate: SERIAL_BAUD_RATE });
            serialWriter = port.writable.getWriter();
            serialPort = port;
            _serialMode = "webserial";

            if (navigator.serial && typeof navigator.serial.addEventListener === "function") {
                navigator.serial.addEventListener("disconnect", handleSerialDisconnect);
            }

            updateSerialIndicator(true);
            await sendSerialCommand("C");
            showToast("Serial connected.", "success");
        } else if (SERIAL_BRIDGE_URL) {
            if (isLikelyMobileDevice()) {
                showToast("Using serial bridge mode on mobile/tablet. Ensure the bridge is running and reachable on the same network.", "success");
            }
            await connectSerialBridge();
        } else {
            showToast(getUnsupportedSerialMessage(), "error");
        }
    } catch (err) {
        if (err && err.name === "NotFoundError") {
            showToast("No serial port selected/found. Check data cable, OTG adapter, USB permission, and USB-serial drivers (CH340/CP210x). If using HC-05/HC-06, use a native app or a serial bridge.", "error");
        } else {
            showToast("Serial error: " + err.message, "error");
        }
    } finally {
        if (btn && !serialPort && !_bridgeConnected) {
            btn.disabled = false;
            btn.textContent = "Connect";
        }
    }
}

async function disconnectSerial() {
    if (!serialPort && !_bridgeConnected) {
        return;
    }

    const btnDisconnect = document.getElementById("btn-serial-disconnect");
    if (btnDisconnect) {
        btnDisconnect.disabled = true;
        btnDisconnect.textContent = "Disconnecting...";
    }

    try {
        if (_serialMode === "bridge") {
            await disconnectSerialBridge();
        } else {
            await sendSerialCommand("D");
            if (serialWriter) {
                await serialWriter.close();
            }
            await serialPort.close();
        }
    } catch (err) {
        showToast("Serial disconnect error: " + err.message, "error");
    } finally {
        serialWriter = null;
        serialPort = null;
        _bridgeConnected = false;
        _serialMode = "none";
        updateSerialIndicator(false);

        if (btnDisconnect) {
            btnDisconnect.textContent = "Disconnect";
        }
    }
}

async function sendSerialMessage(message) {
    if (_serialMode === "bridge") {
        await sendBridgePayload(String(message));
        return;
    }

    if (!serialPort || !serialWriter) {
        return;
    }

    try {
        const data = new TextEncoder().encode(String(message));
        await serialWriter.write(data);
    } catch (err) {
        showToast("Failed to send serial data: " + err.message, "error");
    }
}

async function sendSerialCommand(command) {
    if (_serialMode === "bridge") {
        await sendBridgePayload(command);
        return;
    }

    if (!serialPort || !serialWriter) {
        return;
    }

    // Throttle tone command to avoid saturating the buzzer when digitizing.
    if (command === "T") {
        const now = Date.now();
        if (now - _lastToneTs < SERIAL_TONE_COOLDOWN_MS) {
            return;
        }
        _lastToneTs = now;
    }

    try {
        const data = new TextEncoder().encode(command);
        await serialWriter.write(data);
    } catch (err) {
        showToast("Failed to send serial command: " + err.message, "error");
    }
}

function handleSerialDisconnect() {
    serialWriter = null;
    serialPort = null;
    updateSerialIndicator(false);
    showToast("Serial disconnected.", "error");
}

async function connectSerialBridge() {
    if (!SERIAL_BRIDGE_URL) {
        showToast("Configure SERIAL_BRIDGE_URL to use bridge mode.", "error");
        return;
    }

    if (window.location.protocol === "https:" && SERIAL_BRIDGE_URL.startsWith("http://")) {
        showToast("HTTPS blocks an HTTP bridge. Configure SERIAL_BRIDGE_URL with https://.", "error");
        return;
    }

    const btn = document.getElementById("btn-serial");
    try {
        await bridgeRequest("connect");
        _bridgeConnected = true;
        _serialMode = "bridge";
        updateSerialIndicator(true);
        showToast(`Serial bridge connected (${_activeBridgeBase}).`, "success");
    } catch (err) {
        showToast("Bridge error: " + err.message, "error");
    } finally {
        if (btn && !_bridgeConnected) {
            btn.disabled = false;
            btn.textContent = "Connect";
        }
    }
}

async function disconnectSerialBridge() {
    if (!SERIAL_BRIDGE_URL) {
        return;
    }

    try {
        await bridgeRequest("disconnect", null, { suppressErrors: true });
    } catch (err) {
        showToast("Bridge disconnect error: " + err.message, "error");
    } finally {
        _bridgeConnected = false;
        _serialMode = "none";
    }
}

async function sendBridgePayload(data) {
    if (!SERIAL_BRIDGE_URL || !_bridgeConnected) {
        return;
    }

    try {
        await bridgeRequest("send", { data });
    } catch (err) {
        showToast("Bridge send error: " + err.message, "error");
    }
}

function buildBridgeCandidates(baseUrl) {
    if (!baseUrl) {
        return [];
    }

    const normalized = baseUrl.replace(/\/$/, "");
    const hasPath = (() => {
        try {
            return new URL(normalized).pathname !== "/";
        } catch {
            return false;
        }
    })();

    if (hasPath) {
        return [normalized];
    }

    return [normalized, `${normalized}/serial`];
}

async function bridgeRequest(route, payload, opts = {}) {
    const candidates = _activeBridgeBase ? [_activeBridgeBase] : SERIAL_BRIDGE_CANDIDATES;
    let lastErr = null;

    for (const base of candidates) {
        try {
            const res = await fetch(`${base}/${route}`, {
                method: "POST",
                headers: payload ? { "Content-Type": "application/json" } : undefined,
                body: payload ? JSON.stringify(payload) : undefined
            });

            if (!res.ok) {
                lastErr = new Error(`HTTP ${res.status} at ${base}/${route}`);
                continue;
            }

            _activeBridgeBase = base;
            return;
        } catch (err) {
            lastErr = err;
        }
    }

    if (opts.suppressErrors) {
        return;
    }

    const detail = lastErr && lastErr.message ? lastErr.message : "network/CORS failure";
    throw new Error(`${detail}. Tried: ${SERIAL_BRIDGE_CANDIDATES.join(" and ")}`);
}

updateSerialIndicator(false);
