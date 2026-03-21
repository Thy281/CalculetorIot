// ── App (Main Entry Point) ───────────────────────────────
// Wires up event listeners and orchestrates the calculator workflow.
// Dependencies: api.js, validation.js, ui.js (loaded before this script).

let selectedOperator = "+";

// ── Operator Selection ──────────────────────────────────
function selectOperator(btn) {
	document.querySelectorAll(".op-btn").forEach(b => b.classList.remove("active"));
	btn.classList.add("active");
	selectedOperator = btn.dataset.op;
}

// ── Send Calculation ────────────────────────────────────
async function sendCalculation() {
	const value1 = document.getElementById("value1").value.trim();
	const value2 = document.getElementById("value2").value.trim();
	const base1  = parseInt(document.getElementById("base1").value);
	const base2  = parseInt(document.getElementById("base2").value);

	if (!value1 || !value2) {
		showToast("Please fill in both values.", "error");
		return;
	}

	if (!validateInput(document.getElementById("value1"), "base1") ||
		!validateInput(document.getElementById("value2"), "base2")) {
		showToast("Fix the input errors before calculating.", "error");
		return;
	}

	const btn = document.getElementById("btn-calculate");
	btn.disabled = true;
	btn.textContent = "Calculating...";

	try {
		const data = await postCalculation({
			value1, base1, value2, base2,
			operator: selectedOperator
		});
		document.getElementById("result-display").textContent = data.result;

		// Forward the result to the Arduino when serial is connected.
		if (typeof isSerialConnected === "function" && isSerialConnected()) {
			await sendSerialMessage(String(data.result) + "\n");
			await sendSerialCommand("E"); // beep de enter/calcular
		}

		showToast("Calculation completed successfully!", "success");
		loadHistory();
	} catch (err) {
		showToast("Error: " + err.message, "error");
		document.getElementById("result-display").textContent = "Error";
	} finally {
		btn.disabled = false;
		btn.textContent = "Calculate";
	}
}

// ── AI Smart Calculator ─────────────────────────────────
async function sendAiCalculation() {
	const question = document.getElementById("ai-question").value.trim();

	if (!question) {
		showToast("Please describe your problem first.", "error");
		return;
	}

	const btn = document.getElementById("btn-ai-calculate");
	btn.disabled = true;
	btn.textContent = "Thinking...";
	renderAiLoading();

	try {
		const data = await postAiCalculation(question);
		renderAiResult(data.explanation);
		showToast("AI analysis complete!", "success");
	} catch (err) {
		renderAiError("Failed to get AI response: " + err.message);
		showToast("Error: " + err.message, "error");
	} finally {
		btn.disabled = false;
		btn.textContent = "Solve with AI";
	}
}

// ── Load History ────────────────────────────────────────
async function loadHistory() {
	try {
		const data = await fetchHistory();
		renderHistory(data);
	} catch {
		renderHistoryError();
	}
}

// ── Clear History ───────────────────────────────────────
async function clearHistory() {
	const btn = document.getElementById("btn-clear-history");
	btn.disabled = true;
	btn.textContent = "Clearing...";

	try {
		await deleteHistory();
		renderHistory([]);
		showToast("History cleared successfully!", "success");
	} catch (err) {
		showToast("Error: " + err.message, "error");
	} finally {
		btn.disabled = false;
		btn.textContent = "Clear History";
	}
}

// ── Keyboard Shortcut ───────────────────────────────────
document.addEventListener("keydown", (e) => {
	if (e.key === "Enter" && !e.shiftKey) {
		const active = document.activeElement;
		if (active && (active.id === "value1" || active.id === "value2")) {
			sendCalculation();
		}
	}
	// Ctrl+Enter submits the AI question
	if (e.key === "Enter" && e.ctrlKey) {
		const active = document.activeElement;
		if (active && active.id === "ai-question") {
			sendAiCalculation();
		}
	}
});

// ── Init ────────────────────────────────────────────────
loadHistory();

// Tone feedback on typing for accessibility (throttled inside sendSerialCommand)
["value1", "value2", "ai-question"].forEach(id => {
	const el = document.getElementById(id);
	if (el) {
		el.addEventListener("input", () => {
			if (typeof sendSerialCommand === "function") {
				sendSerialCommand("T");
			}
		});
	}
});

