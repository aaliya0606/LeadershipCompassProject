const API_BASE = "http://localhost:8080/api/admin/modules";
const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

const modulesTableBody = document.getElementById("modulesTableBody");
const moduleCount = document.getElementById("moduleCount");
const pageAlert = document.getElementById("pageAlert");
const sortBy = document.getElementById("sortBy");
const sortDirection = document.getElementById("sortDirection");
const refreshBtn = document.getElementById("refreshBtn");
const addModuleBtn = document.getElementById("addModuleBtn");
const logoutBtn = document.getElementById("logoutBtn");
const moduleForm = document.getElementById("moduleForm");
const moduleModalEl = document.getElementById("moduleModal");
const moduleModalLabel = document.getElementById("moduleModalLabel");
const formAlert = document.getElementById("formAlert");

const moduleModal = new bootstrap.Modal(moduleModalEl);

let editingModuleId = null;

if (!token || role !== "ADMIN") {
  window.location.href = "index.html";
}

function authHeaders() {
  return {
    Authorization: "Bearer " + token,
    "Content-Type": "application/json"
  };
}

function showPageAlert(message, type) {
  pageAlert.textContent = message;
  pageAlert.className = "alert alert-" + type;
  pageAlert.classList.remove("d-none");
}

function hidePageAlert() {
  pageAlert.classList.add("d-none");
}

function showFormAlert(message) {
  formAlert.textContent = message;
  formAlert.classList.remove("d-none");
}

function hideFormAlert() {
  formAlert.classList.add("d-none");
}

function linesToList(value) {
  return value
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line.length > 0);
}

function listToLines(values) {
  if (!values || values.length === 0) {
    return "";
  }
  return values.join("\n");
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text ?? "";
  return div.innerHTML;
}

async function loadModules() {
  hidePageAlert();
  modulesTableBody.innerHTML =
    '<tr><td colspan="7" class="text-muted">Loading modules...</td></tr>';

  const params = new URLSearchParams({
    sortBy: sortBy.value,
    direction: sortDirection.value
  });

  try {
    const response = await fetch(`${API_BASE}?${params.toString()}`, {
      headers: authHeaders()
    });

    if (!response.ok) {
      throw new Error(await response.text());
    }

    const modules = await response.json();
    renderModules(modules);
  } catch (error) {
    modulesTableBody.innerHTML =
      '<tr><td colspan="7" class="text-danger">Failed to load modules.</td></tr>';
    showPageAlert("Could not load modules. Check that you are logged in as an admin and the backend is running.", "danger");
    console.error(error);
  }
}

function renderModules(modules) {
  moduleCount.textContent = modules.length + " module" + (modules.length === 1 ? "" : "s");

  if (modules.length === 0) {
    modulesTableBody.innerHTML =
      '<tr><td colspan="7" class="text-muted">No modules found.</td></tr>';
    return;
  }

  modulesTableBody.innerHTML = modules
    .map(
      (module) => `
      <tr>
        <td>${module.id}</td>
        <td>${escapeHtml(module.category)}</td>
        <td>${escapeHtml(module.book)}</td>
        <td>${escapeHtml(module.title)}</td>
        <td>${module.displayOrder ?? ""}</td>
        <td>
          <span class="badge ${module.active ? "bg-success" : "bg-secondary"}">
            ${module.active ? "Yes" : "No"}
          </span>
        </td>
        <td class="text-end text-nowrap">
          <button type="button" class="btn btn-sm btn-outline-primary me-1" data-action="edit" data-id="${module.id}">
            Edit
          </button>
          <button type="button" class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${module.id}">
            Delete
          </button>
        </td>
      </tr>`
    )
    .join("");
}

function resetForm() {
  editingModuleId = null;
  moduleForm.reset();
  document.getElementById("moduleCategory").value = "";
  document.getElementById("moduleActive").checked = true;
  document.getElementById("moduleDisplayOrder").value = "0";
  hideFormAlert();
}

function openCreateModal() {
  resetForm();
  moduleModalLabel.textContent = "Add learning module";
  moduleModal.show();
}

async function openEditModal(id) {
  hideFormAlert();
  moduleModalLabel.textContent = "Edit learning module";

  try {
    const response = await fetch(`${API_BASE}/${id}`, {
      headers: authHeaders()
    });

    if (!response.ok) {
      throw new Error(await response.text());
    }

    const module = await response.json();
    editingModuleId = module.id;

    document.getElementById("moduleCategory").value = module.category || "";
    document.getElementById("moduleBook").value = module.book || "";
    document.getElementById("moduleTitle").value = module.title || "";
    document.getElementById("moduleDescription").value = module.description || "";
    document.getElementById("moduleDisplayOrder").value = module.displayOrder ?? 0;
    document.getElementById("moduleActive").checked = Boolean(module.active);
    document.getElementById("moduleSourceChapters").value = listToLines(module.sourceChapters);
    document.getElementById("moduleChecklist").value = listToLines(module.checklist);
    document.getElementById("moduleQuotes").value = listToLines(module.quotesAndConcepts);
    document.getElementById("moduleActivities").value = listToLines(module.activities);

    moduleModal.show();
  } catch (error) {
    showPageAlert("Could not load module details for editing.", "danger");
    console.error(error);
  }
}

function buildPayload() {
  return {
    category: document.getElementById("moduleCategory").value.trim(),
    book: document.getElementById("moduleBook").value.trim(),
    title: document.getElementById("moduleTitle").value.trim(),
    description: document.getElementById("moduleDescription").value.trim(),
    displayOrder: Number(document.getElementById("moduleDisplayOrder").value),
    active: document.getElementById("moduleActive").checked,
    sourceChapters: linesToList(document.getElementById("moduleSourceChapters").value),
    checklist: linesToList(document.getElementById("moduleChecklist").value),
    quotesAndConcepts: linesToList(document.getElementById("moduleQuotes").value),
    activities: linesToList(document.getElementById("moduleActivities").value)
  };
}

async function saveModule(event) {
  event.preventDefault();
  hideFormAlert();

  const payload = buildPayload();
  const isEdit = editingModuleId !== null;
  const url = isEdit ? `${API_BASE}/${editingModuleId}` : API_BASE;
  const method = isEdit ? "PUT" : "POST";

  try {
    const response = await fetch(url, {
      method,
      headers: authHeaders(),
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const errorText = await response.text();
      showFormAlert(errorText || "Unable to save module.");
      return;
    }

    moduleModal.hide();
    showPageAlert(isEdit ? "Module updated successfully." : "Module created successfully.", "success");
    await loadModules();
  } catch (error) {
    showFormAlert("Could not connect to the backend.");
    console.error(error);
  }
}

async function deleteModule(id) {
  const confirmed = window.confirm("Delete this learning module? This cannot be undone.");
  if (!confirmed) {
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/${id}`, {
      method: "DELETE",
      headers: authHeaders()
    });

    if (!response.ok) {
      throw new Error(await response.text());
    }

    showPageAlert("Module deleted successfully.", "success");
    await loadModules();
  } catch (error) {
    showPageAlert("Could not delete module.", "danger");
    console.error(error);
  }
}

modulesTableBody.addEventListener("click", function (event) {
  const button = event.target.closest("button[data-action]");
  if (!button) {
    return;
  }

  const id = button.getAttribute("data-id");
  if (button.getAttribute("data-action") === "edit") {
    openEditModal(id);
  } else if (button.getAttribute("data-action") === "delete") {
    deleteModule(id);
  }
});

sortBy.addEventListener("change", loadModules);
sortDirection.addEventListener("change", loadModules);
refreshBtn.addEventListener("click", loadModules);
addModuleBtn.addEventListener("click", openCreateModal);
moduleForm.addEventListener("submit", saveModule);

logoutBtn.addEventListener("click", function () {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  window.location.href = "index.html";
});

loadModules();
