// ── UI Module ────────────────────────────────────────────
// Toast notifications and DOM helper utilities.

/**
 * Escapes a string for safe insertion into HTML.
 * @param {string} str
 * @returns {string}
 */
function escapeHtml(str) {
    const div = document.createElement("div");
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}

/**
 * Shows a temporary toast notification.
 * @param {string} message - The message to display.
 * @param {"success"|"error"} type - Visual style of the toast.
 */
function showToast(message, type) {
    const container = document.getElementById("toast-container");
    const colors = {
        success: "bg-emerald-900/80 border-emerald-600/40 text-emerald-300",
        error:   "bg-red-900/80 border-red-600/40 text-red-300"
    };
    const toast = document.createElement("div");
    toast.className = `toast px-4 py-3 rounded-lg border text-sm backdrop-blur-sm ${colors[type] || colors.error}`;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

/** Base-name look-up for history table badges. */
const BASE_NAME = { 2: "BIN", 8: "OCT", 10: "DEC", 16: "HEX" };

/** Operator-symbol look-up for history table display. */
const OP_SYMBOL = { "+": "+", "-": "−", "*": "×", "/": "÷" };

/**
 * Renders an array of Calculation rows into the history table body.
 * @param {object[]} data - Array of Calculation entities from the API.
 */
function renderHistory(data) {
    const tbody = document.getElementById("history-body");

    if (!data.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="px-6 py-12 text-center text-gray-600 text-sm">
                    <div class="flex flex-col items-center gap-2">
                        <svg class="w-8 h-8 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
                        </svg>
                        No operations recorded yet
                    </div>
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = data
        .slice()
        .reverse()
        .map(row => {
            const date = row.timestamp
                ? new Date(row.timestamp).toLocaleString("en-US", {
                    month: "short", day: "2-digit",
                    hour: "2-digit", minute: "2-digit"
                  })
                : "—";

            const v1 = (row.value1 || "").toUpperCase();
            const v2 = (row.value2 || "").toUpperCase();

            return `
                <tr class="table-row border-b border-gray-800/30 transition-colors">
                    <td class="px-6 py-3">
                        <span class="mono text-cyan-300">${escapeHtml(v1)}</span>
                        <span class="ml-1.5 text-[10px] px-1.5 py-0.5 rounded bg-gray-800 text-gray-500 font-semibold">${BASE_NAME[row.base1] || row.base1}</span>
                    </td>
                    <td class="px-4 py-3 text-center">
                        <span class="mono text-lg text-gray-400">${OP_SYMBOL[row.operator] || escapeHtml(row.operator)}</span>
                    </td>
                    <td class="px-6 py-3">
                        <span class="mono text-cyan-300">${escapeHtml(v2)}</span>
                        <span class="ml-1.5 text-[10px] px-1.5 py-0.5 rounded bg-gray-800 text-gray-500 font-semibold">${BASE_NAME[row.base2] || row.base2}</span>
                    </td>
                    <td class="px-6 py-3 text-right">
                        <span class="mono font-bold text-emerald-400">${escapeHtml(String(row.result))}</span>
                    </td>
                    <td class="px-6 py-3 text-right text-gray-500 text-xs">${escapeHtml(date)}</td>
                </tr>`;
        })
        .join("");
}

/**
 * Renders an error state in the history table.
 */
function renderHistoryError() {
    document.getElementById("history-body").innerHTML = `
        <tr>
            <td colspan="5" class="px-6 py-8 text-center text-red-400/70 text-sm">
                Failed to load history — is the server running?
            </td>
        </tr>`;
}

/**
 * Shows the AI result panel in a loading state.
 */
function renderAiLoading() {
    const panel = document.getElementById("ai-result-panel");
    const content = document.getElementById("ai-result-content");
    panel.classList.remove("hidden");
    content.innerHTML = `
        <div class="flex items-center gap-3 text-cyan-400/70">
            <svg class="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
            </svg>
            <span class="text-sm text-gray-400">Analyzing your problem...</span>
        </div>`;
}

/**
 * Renders the AI explanation using marked.js (loaded from CDN).
 * @param {string} explanation - Markdown text returned by the AI.
 */
function renderAiResult(explanation) {
    const panel = document.getElementById("ai-result-panel");
    const content = document.getElementById("ai-result-content");
    panel.classList.remove("hidden");
    // marked is loaded via CDN in index.html
    content.innerHTML = typeof marked !== "undefined"
        ? marked.parse(explanation)
        : `<pre class="whitespace-pre-wrap text-sm text-gray-300">${escapeHtml(explanation)}</pre>`;
}

/**
 * Renders an error inside the AI result panel.
 * @param {string} message
 */
function renderAiError(message) {
    const panel = document.getElementById("ai-result-panel");
    const content = document.getElementById("ai-result-content");
    panel.classList.remove("hidden");
    content.innerHTML = `<p class="text-red-400 text-sm">${escapeHtml(message)}</p>`;
}

