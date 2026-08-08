const token = localStorage.getItem("token");
const form = document.getElementById("planPreviewForm");
const previewSubmitBtn = document.getElementById("previewSubmitBtn");
const previewWeeksContainer = document.getElementById("previewWeeksContainer");
const planSummary = document.getElementById("planSummary");
const pocAlert = document.getElementById("pocAlert");
const logoutBtn = document.getElementById("logoutBtn");

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

if (form) {
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    previewSubmitBtn.disabled = true;
    showAlert("Sending mock scores and active modules to the AI-brain...", "info");

    const payload = {
      fullName: document.getElementById("fullName").value.trim(),
      caringTimeScore: Number(document.getElementById("caringTimeScore").value),
      receivingValueScore: Number(document.getElementById("receivingValueScore").value),
      actsOfSupportScore: Number(document.getElementById("actsOfSupportScore").value),
      wordsOfRecognitionScore: Number(document.getElementById("wordsOfRecognitionScore").value),
      psychologicalTouchScore: Number(document.getElementById("psychologicalTouchScore").value)
    };

    try {
      const response = await fetch("http://localhost:8080/api/development-plans/preview", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        throw new Error(await extractError(response));
      }

      const plan = await response.json();
      renderPlan(plan);
      showAlert(`Preview returned using ${formatGenerationSource(plan.generationSource)}.`, "success");
    } catch (error) {
      showAlert(error.message || "Unable to generate a preview right now.", "danger");
    } finally {
      previewSubmitBtn.disabled = false;
    }
  });
}

function renderPlan(plan) {
  planSummary.textContent = `Plan generated ${formatTimestamp(plan.generatedAt)} using ${formatGenerationSource(plan.generationSource)}.`;

  previewWeeksContainer.innerHTML = (plan.weeks || [])
    .map((week) => {
      const actions = (week.actions || [])
        .map((action) => `<li class="list-group-item">${escapeHtml(action)}</li>`)
        .join("");

      return `
        <div class="col-md-6">
          <div class="card week-card h-100">
            <div class="card-body">
              <h5>Week ${week.weekNumber}: ${escapeHtml(week.moduleTitle)}</h5>
              <p class="text-muted mb-2">${escapeHtml(week.category)}</p>
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
  pocAlert.textContent = message;
  pocAlert.className = `alert alert-${type}`;
  pocAlert.classList.remove("d-none");
}

function formatGenerationSource(source) {
  return source === "AI_BRAIN" ? "the AI-brain" : "the fallback planner";
}

function formatTimestamp(value) {
  if (!value) {
    return "just now";
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "just now" : date.toLocaleString();
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}
