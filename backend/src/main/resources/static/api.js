// ── API Module ───────────────────────────────────────────
// Handles all communication with the Spring Boot back-end.

// Prefer same-origin API by default; allow override via window.API_BASE for custom hosts.
const API_BASE = (window.API_BASE || (window.location.origin + "/api/calculations"));

/**
 * POST /api/calculations — sends a calculation request.
 * @param {{ value1: string, base1: number, value2: string, base2: number, operator: string }} payload
 * @returns {Promise<object>} The saved Calculation entity from the server.
 */
async function postCalculation(payload) {
    const response = await fetch(API_BASE, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || "Calculation failed");
    }

    return response.json();
}

/**
 * GET /api/calculations/history — fetches all past calculations.
 * @returns {Promise<object[]>} Array of Calculation entities.
 */
async function fetchHistory() {
    const response = await fetch(API_BASE + "/history");

    if (!response.ok) {
        throw new Error("Failed to load history");
    }

    return response.json();
}

/**
 * DELETE /api/calculations/history — deletes all calculation history.
 * @returns {Promise<void>}
 */
async function deleteHistory() {
    const response = await fetch(API_BASE + "/history", {
        method: "DELETE"
    });

    if (!response.ok) {
        throw new Error("Failed to clear history");
    }
}

/**
 * POST /api/calculations/ai — sends a natural-language math problem to the AI.
 * @param {string} question - The natural-language problem to solve.
 * @returns {Promise<{ question: string, explanation: string }>}
 */
async function postAiCalculation(question) {
    const response = await fetch(API_BASE + "/ai", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ question })
    });

    if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || "AI calculation failed");
    }

    return response.json();
}

