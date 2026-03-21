// ── Ultrathink Base Converter Module ───────────────────────
// Handles base-to-base conversion UI and logic.

// Generate base options from 2 to 36
function generateBaseOptions() {
    const fragment = document.createDocumentFragment();
    for (let base = 2; base <= 36; base++) {
        const option = document.createElement("option");
        option.value = base;
        let label;
        switch (base) {
            case 2: label = "BIN (2)"; break;
            case 8: label = "OCT (8)"; break;
            case 10: label = "DEC (10)"; break;
            case 16: label = "HEX (16)"; break;
            default: label = `BASE ${base}`;
        }
        option.textContent = label;
        fragment.appendChild(option);
    }
    return fragment;
}

// Initialize converter UI
function initConverter() {
    const fromBaseSelect = document.getElementById("conv-from-base");
    const toBaseSelect = document.getElementById("conv-to-base");

    if (fromBaseSelect && toBaseSelect) {
        fromBaseSelect.innerHTML = "";
        toBaseSelect.innerHTML = "";
        fromBaseSelect.appendChild(generateBaseOptions());
        toBaseSelect.appendChild(generateBaseOptions());

        // Set default values
        fromBaseSelect.value = 16;
        toBaseSelect.value = 2;

        // Swap bases when button clicked
        document.getElementById("conv-swap-bases")?.addEventListener("click", () => {
            const temp = fromBaseSelect.value;
            fromBaseSelect.value = toBaseSelect.value;
            toBaseSelect.value = temp;
            // Also swap the input values
            const inputValue = document.getElementById("conv-value").value;
            // We don't swap the input, just the bases - let user re-enter if needed
        });

        // Convert on Enter key
        document.getElementById("conv-value")?.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                performConversion();
            }
        });

        // Live conversion as user types (debounced)
        let debounceTimer;
        document.getElementById("conv-value")?.addEventListener("input", () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                performConversion();
            }, 300);
        });
    }
}

// Perform the base conversion
async function performConversion() {
    const value = document.getElementById("conv-value").value.trim();
    const fromBase = parseInt(document.getElementById("conv-from-base").value);
    const toBase = parseInt(document.getElementById("conv-to-base").value);
    const resultEl = document.getElementById("conv-result");
    const errorEl = document.getElementById("conv-error");

    // Clear previous error
    errorEl.classList.add("hidden");

    if (!value) {
        resultEl.textContent = "—";
        return;
    }

    // Validate input
    const inputEl = document.getElementById("conv-value");
    if (!validateConverterInput(value, fromBase)) {
        resultEl.textContent = "—";
        return;
    }

    try {
        const response = await postConvert({ value, fromBase, toBase });
        resultEl.textContent = response.convertedValue;
        resultEl.classList.remove("text-gray-500");
        resultEl.classList.add("text-amber-400");
    } catch (err) {
        errorEl.textContent = err.message || "Conversion failed";
        errorEl.classList.remove("hidden");
        resultEl.textContent = "—";
    }
}

// Validate converter input (same logic as validation.js but for converter section)
function validateConverterInput(value, base) {
    if (!value) return true;
    const allowedChars = VALID_CHARS.substring(0, base);
    const invalidChars = value.toUpperCase().split('').filter(c => !allowedChars.includes(c));
    if (invalidChars.length > 0) {
        const baseName = getBaseName(base);
        const maxDigit = getMaxDigit(base);
        // Use the error element in the converter section
        const errorEl = document.getElementById("conv-error");
        if (errorEl) {
            errorEl.textContent = `${baseName} accepts only digits 0-${maxDigit}`;
            errorEl.classList.remove("hidden");
        }
        showToast(`${baseName} accepts only digits 0-${maxDigit}`, "error");
        return false;
    }
    return true;
}

// Initialize when DOM is ready
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initConverter);
} else {
    initConverter();
}
