const API_BASE = "http://localhost:8080/api/development-plans";
const token = localStorage.getItem("token");

const logoutBtn = document.getElementById("logoutBtn");
const generatePlanBtn = document.getElementById("generatePlanBtn");
const weeksContainer = document.getElementById("weeksContainer");
const planMeta = document.getElementById("planMeta");
const planAlert = document.getElementById("planAlert");
const planLoading = document.getElementById("planLoading");
const planProgress = document.getElementById("planProgress");
const planProgressLabel = document.getElementById("planProgressLabel");
const planProgressFill = document.getElementById("planProgressFill");

let currentPlan = null;

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

if (weeksContainer) {
  weeksContainer.addEventListener("change", (event) => {
    const checkbox = event.target;
    if (!(checkbox instanceof HTMLInputElement) || !checkbox.matches("[data-action-checkbox]")) {
      return;
    }
    toggleAction(checkbox);
  });
}

loadCurrentPlan();

async function loadCurrentPlan() {
  planLoading.classList.remove("d-none");
  hideProgress();
  try {
    const response = await fetch(`${API_BASE}/current`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    if (response.status === 404) {
      hideProgress();
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
  currentPlan = plan;
  const weeksRaw = Array.isArray(plan.weeks) ? plan.weeks : [];
  if (weeksRaw.length === 0) {
    hideProgress();
    weeksContainer.innerHTML = emptyState("No weekly recommendations were returned.", "Generate the plan again after survey data is available.");
    return;
  }

  planMeta.textContent = `Generated ${formatTimestamp(plan.generatedAt)} using ${formatGenerationSource(plan.generationSource)}. Complete modules in week order (1 → 5).`;

  const weeks = [...weeksRaw].sort((a, b) => (a.weekNumber || 0) - (b.weekNumber || 0));
  updateProgress(weeks);
  weeksContainer.innerHTML = weeks
    .map((week) => {
      const actions = normalizeActions(week.actions);
      const completedCount = actions.filter((action) => action.completed).length;
      const actionItems = actions
        .map((action) => {
          const inputId = `action-${week.weekNumber}-${action.index}`;
          const completeClass = action.completed ? " is-complete" : "";
          const checked = action.completed ? "checked" : "";
          return `
            <li class="list-group-item">
              <div class="action-check-item${completeClass}">
                <input
                  id="${inputId}"
                  type="checkbox"
                  data-action-checkbox
                  data-week-number="${week.weekNumber}"
                  data-action-index="${action.index}"
                  ${checked}
                />
                <label for="${inputId}">${escapeHtml(action.text)}</label>
              </div>
            </li>`;
        })
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
                  <span class="badge text-bg-light d-block mb-1">Module ${week.moduleId ?? "-"}</span>
                  <span class="badge badge-soft-success week-progress-badge">${completedCount} of ${actions.length} actions</span>
                </div>
              </div>
              <p><strong>Focus:</strong> ${escapeHtml(week.focus || "No focus provided.")}</p>
              <p class="text-muted">${escapeHtml(week.rationale || "No rationale provided.")}</p>
              <ul class="list-group list-group-flush">
                ${actionItems || '<li class="list-group-item">No action items provided.</li>'}
              </ul>
            </div>
          </div>
        </div>`;
    })
    .join("");
}

async function toggleAction(checkbox) {
  if (!currentPlan || !currentPlan.id) {
    checkbox.checked = !checkbox.checked;
    showAlert("This plan cannot be updated yet. Generate a plan first.", "danger");
    return;
  }

  const weekNumber = Number(checkbox.dataset.weekNumber);
  const actionIndex = Number(checkbox.dataset.actionIndex);
  const completed = checkbox.checked;
  checkbox.disabled = true;

  try {
    const response = await fetch(
      `${API_BASE}/${currentPlan.id}/weeks/${weekNumber}/actions/${actionIndex}`,
      {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ completed })
      }
    );

    if (!response.ok) {
      throw new Error(await extractError(response));
    }

    renderPlan(await response.json());
  } catch (error) {
    checkbox.checked = !completed;
    showAlert(error.message || "Unable to update that action right now.", "danger");
  } finally {
    checkbox.disabled = false;
  }
}

function normalizeActions(actions) {
  return (actions || []).map((action, index) => {
    if (typeof action === "string") {
      return { index, text: action, completed: false };
    }
    return {
      index: action.index ?? index,
      text: action.text || "",
      completed: Boolean(action.completed)
    };
  });
}

function updateProgress(weeks) {
  const actions = weeks.flatMap((week) => normalizeActions(week.actions));
  const total = actions.length;
  const completed = actions.filter((action) => action.completed).length;
  const percent = total === 0 ? 0 : Math.round((completed / total) * 100);

  planProgress.classList.remove("d-none");
  planProgressLabel.textContent = `${completed} of ${total} actions complete`;
  planProgressFill.style.width = `${percent}%`;
  const track = planProgress.querySelector("[role='progressbar']");
  if (track) {
    track.setAttribute("aria-valuenow", String(percent));
  }
}

function hideProgress() {
  currentPlan = null;
  planProgress.classList.add("d-none");
  planProgressFill.style.width = "0%";
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
