// ── Speech Module ─────────────────────────────────────────
// Web Speech API: voice-to-text for calculator and AI inputs.

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

let _recognition    = null;
let _activeTargetId = null;
let _activeBtnId    = null;

/**
 * Toggles speech recognition on/off for the given input element.
 * @param {string} targetId - The id of the input/textarea to populate.
 * @param {string} btnId    - The id of the mic button triggering the action.
 */
function toggleMic(targetId, btnId) {
    if (!SpeechRecognition) {
        showToast("Speech recognition is not supported in this browser.", "error");
        return;
    }

    const btn    = document.getElementById(btnId);
    const target = document.getElementById(targetId);

    // Already listening for this target → stop
    if (_recognition && _activeTargetId === targetId) {
        _recognition.stop();
        return;
    }

    // Listening for a different target → abort the previous session first
    if (_recognition) {
        _recognition.abort();
    }

    _activeTargetId = targetId;
    _activeBtnId    = btnId;

    _recognition                = new SpeechRecognition();
    _recognition.lang           = "pt-BR";
    _recognition.interimResults = true;
    _recognition.maxAlternatives = 1;
    _recognition.continuous     = false;

    _recognition.onstart = () => {
        btn.classList.add("mic-active");
        btn.setAttribute("title", "Click to stop recording");
    };

    _recognition.onresult = (event) => {
        const transcript = Array.from(event.results)
            .map(r => r[0].transcript)
            .join("");
        target.value = transcript;
        // Trigger validation / reactivity on the target field
        target.dispatchEvent(new Event("input"));
    };

    _recognition.onerror = (event) => {
        if (event.error !== "aborted") {
            showToast("Microphone error: " + event.error, "error");
        }
        _clearMicState(btn);
    };

    _recognition.onend = () => {
        _clearMicState(btn);
    };

    _recognition.start();
}

function _clearMicState(btn) {
    if (btn) {
        btn.classList.remove("mic-active");
        btn.setAttribute("title", "Voice input");
    }
    _recognition    = null;
    _activeTargetId = null;
    _activeBtnId    = null;
}
