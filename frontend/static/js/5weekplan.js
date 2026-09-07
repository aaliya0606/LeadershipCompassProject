const API_BASE = "http://localhost:8080/api/development-plans";
const token = localStorage.getItem("token");

const logoutBtn = document.getElementById("logoutBtn");
const generatePlanBtn = document.getElementById("generatePlanBtn");
const weeksContainer = document.getElementById("weeksContainer");
const planMeta = document.getElementById("planMeta");
const planAlert = document.getElementById("planAlert");
const planLoading = document.getElementById("planLoading");

if (!token) {
  window.location.href = "index.html";
}

if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "index.html";
  });
}

if (generatePlanBtn) {
  generatePlanBtn.addEventListener("click", async () => {
    generatePlanBtn.disabled = true;
    showAlert("Generating your personalised plan...", "info");
    try {
      const response = await fetch(`${API_BASE}/generate`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error(await extractError(response));
      }

      const plan = await response.json();
      renderPlan(plan);
      showAlert("Your plan has been generated.", "success");
    } catch (error) {
      showAlert(error.message || "Unable to generate a development plan right now.", "danger");
    } finally {
      generatePlanBtn.disabled = false;
    }
  });
}

loadCurrentPlan();

async function loadCurrentPlan() {
  planLoading.classList.remove("d-none");
  try {
    const response = await fetch(`${API_BASE}/current`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    if (response.status === 404) {
      weeksContainer.innerHTML = emptyState(
        "No plan has been generated yet.",
        "Use the button above to create a personalised 5-week plan from your latest survey scores."
      );
      planMeta.textContent = "No saved plan found yet.";
      return;
    }

    if (!response.ok) {
      throw new Error(await extractError(response));
    }

    renderPlan(await response.json());
  } catch (error) {
    weeksContainer.innerHTML = emptyState(
      "Plan unavailable",
      error.message || "Unable to load the current development plan."
    );
  } finally {
    planLoading.classList.add("d-none");
  }
}

function renderPlan(plan) {
  const weeksRaw = Array.isArray(plan.weeks) ? plan.weeks : [];
  if (weeksRaw.length === 0) {
    weeksContainer.innerHTML = emptyState("No weekly recommendations were returned.", "Generate the plan again after survey data is available.");
    return;
  }

  planMeta.textContent = `Generated ${formatTimestamp(plan.generatedAt)} using ${formatGenerationSource(plan.generationSource)}. Complete modules in week order (1 → 5).`;

  const weeks = [...weeksRaw].sort((a, b) => (a.weekNumber || 0) - (b.weekNumber || 0));
  weeksContainer.innerHTML = weeks
    .map((week) => {
      const actions = (week.actions || [])
        .map((action) => `<li class="list-group-item">${escapeHtml(action)}</li>`)
        .join("");

      return `
        <div class="col-lg-6">
          <div class="card week-card h-100">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                <div>
                  <h5>Week ${week.weekNumber}: ${escapeHtml(week.moduleTitle)}</h5>
                  <p class="text-muted mb-0">${escapeHtml(week.category)}</p>
                </div>
                <div class="text-end">
                  <span class="badge text-bg-secondary d-block mb-1">Step ${week.weekNumber} of 5</span>
                  <span class="badge text-bg-light">Module ${week.moduleId ?? "-"}</span>
                </div>
              </div>
              <p><strong>Focus:</strong> ${escapeHtml(week.focus || "No focus provided.")}</p>
              <p class="text-muted">${escapeHtml(week.rationale || "No rationale provided.")}</p>
              <ul class="list-group list-group-flush">
                ${actions || '<li class="list-group-item">No action items provided.</li>'}
              </ul>
            </div>
          </div>
        </div>`;
    })
    .join("");
}

async function extractError(response) {
  try {
    const data = await response.json();
    return data.message || data.error || `Request failed with status ${response.status}`;
  } catch (error) {
    return `Request failed with status ${response.status}`;
  }
}

function showAlert(message, type) {
  planAlert.textContent = message;
  planAlert.className = `alert alert-${type}`;
  planAlert.classList.remove("d-none");
}

function emptyState(title, message) {
  return `
    <div class="col-12">
      <div class="card p-4">
        <h5>${escapeHtml(title)}</h5>
        <p class="text-muted mb-0">${escapeHtml(message)}</p>
      </div>
    </div>`;
}

function formatGenerationSource(source) {
  return source === "AI_BRAIN" ? "the AI brain" : "the fallback planner";
}

function formatTimestamp(value) {
  if (!value) {
    return "recently";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "recently";
  }
  return date.toLocaleString();
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}
