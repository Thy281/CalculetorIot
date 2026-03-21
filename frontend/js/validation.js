// ── Validation Module ────────────────────────────────────
// Input validation rules per numeric base (2-36).

const VALID_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

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

    // Validate characters based on base
    const allowedChars = VALID_CHARS.substring(0, base);
    const invalidChars = value.toUpperCase().split('').filter(c => !allowedChars.includes(c));

    if (invalidChars.length > 0) {
        const invalidChar = invalidChars[0];
        const baseName = getBaseName(base);
        const maxDigit = getMaxDigit(base);
        errorEl.textContent = `${baseName} accepts only digits 0-${maxDigit}`;
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

function getBaseName(base) {
    const names = {
        2: "Binary",
        8: "Octal",
        10: "Decimal",
        16: "Hexadecimal"
    };
    return names[base] || `Base ${base}`;
}

function getMaxDigit(base) {
    if (base <= 10) {
        return base - 1;
    }
    // For bases > 10, show letters for digits >= 10
    if (base <= 36) {
        const maxIndex = base - 1;
        return VALID_CHARS[maxIndex];
    }
    return "";
}
