// ── Validation Module ────────────────────────────────────
// Input validation rules per numeric base.

const BASE_RULES = {
    2:  { pattern: /[^01]/i,    message: "Binary accepts only 0 and 1" },
    8:  { pattern: /[^0-7]/i,   message: "Octal accepts only digits 0-7" },
    10: { pattern: /[^0-9]/i,   message: "Decimal accepts only digits 0-9" },
    16: { pattern: /[^0-9a-f]/i, message: "Hexadecimal accepts only 0-9 and A-F" }
};

/**
 * Validates an input field against the selected base rules.
 * Shows/hides the corresponding error message and applies error styling.
 *
 * @param {HTMLInputElement} input - The text input element.
 * @param {string} baseSelectId - The id of the associated <select> element.
 * @returns {boolean} Whether the current value is valid.
 */
function validateInput(input, baseSelectId) {
    const base = parseInt(document.getElementById(baseSelectId).value);
    const value = input.value.trim();
    const errorEl = baseSelectId === "base1"
        ? document.getElementById("error1")
        : document.getElementById("error2");

    if (!value) {
        errorEl.classList.add("hidden");
        input.classList.remove("border-red-500/50");
        return true;
    }

    const rule = BASE_RULES[base];
    if (rule && rule.pattern.test(value)) {
        errorEl.textContent = rule.message;
        errorEl.classList.remove("hidden");
        input.classList.add("border-red-500/50");
        input.classList.add("shake");
        setTimeout(() => input.classList.remove("shake"), 300);
        return false;
    }

    errorEl.classList.add("hidden");
    input.classList.remove("border-red-500/50");
    return true;
}
