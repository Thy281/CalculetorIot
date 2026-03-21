// ── Serial Module ─────────────────────────────────────────
// Web Serial API: connect to Arduino and send data.

const SERIAL_BAUD_RATE = 9600;
let serialPort = null;
let serialWriter = null;
let _lastToneTs = 0;
const SERIAL_TONE_COOLDOWN_MS = 120;

function isSerialSupported() {
    return "serial" in navigator && typeof navigator.serial.requestPort === "function";
}

function isSerialConnected() {
    return !!serialPort;
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
    if (!isSerialSupported()) {
        showToast("Web Serial API not supported in this browser.", "error");
        return;
    }

    if (serialPort) {
        showToast("Serial already connected.", "success");
        return;
    }

    const btn = document.getElementById("btn-serial");
    if (btn) {
        btn.disabled = true;
        btn.textContent = "Connecting...";
    }

    try {
        const port = await navigator.serial.requestPort();
        await port.open({ baudRate: SERIAL_BAUD_RATE });
        serialWriter = port.writable.getWriter();
        serialPort = port;

        if (navigator.serial && typeof navigator.serial.addEventListener === "function") {
            navigator.serial.addEventListener("disconnect", handleSerialDisconnect);
        }

        updateSerialIndicator(true);
        await sendSerialCommand("C"); // signal connected
        showToast("Serial connected.", "success");
    } catch (err) {
        showToast("Serial error: " + err.message, "error");
    } finally {
        if (btn && !serialPort) {
            btn.disabled = false;
            btn.textContent = "Connect";
        }
    }
}

async function disconnectSerial() {
    if (!serialPort) {
        return;
    }

    const btnDisconnect = document.getElementById("btn-serial-disconnect");
    if (btnDisconnect) {
        btnDisconnect.disabled = true;
        btnDisconnect.textContent = "Disconnecting...";
    }

    try {
        await sendSerialCommand("D"); // signal disconnect
        if (serialWriter) {
            await serialWriter.close();
        }
        await serialPort.close();
    } catch (err) {
        showToast("Serial disconnect error: " + err.message, "error");
    } finally {
        serialWriter = null;
        serialPort = null;
        updateSerialIndicator(false);

        if (btnDisconnect) {
            btnDisconnect.textContent = "Disconnect";
        }
    }
}

async function sendSerialMessage(message) {
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
    if (!serialPort || !serialWriter) {
        return;
    }

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

updateSerialIndicator(false);
