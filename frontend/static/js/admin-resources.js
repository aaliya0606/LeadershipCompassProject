/**
 * Admin Resource Library management interface.
 *
 * Handles authenticated resource loading, filtering, secure file viewing,
 * uploads, metadata editing, and deletion through the Resource API.
 *
 * This page is intended for authenticated ADMIN users only.
 */

// ========================================
// Authentication and shared state
// ========================================

const API_BASE = "http://localhost:8080/api/resources";

const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

const resourcesTableBody = document.getElementById("resourcesTableBody");
const resourceCount = document.getElementById("resourceCount");
const pageAlert = document.getElementById("pageAlert");
const formAlert = document.getElementById("formAlert");

const filterLanguage = document.getElementById("filterLanguage");
const filterType = document.getElementById("filterType");
const refreshBtn = document.getElementById("refreshBtn");
const addResourceBtn = document.getElementById("addResourceBtn");
const logoutBtn = document.getElementById("logoutBtn");

const resourceForm = document.getElementById("resourceForm");
const resourceModalEl = document.getElementById("resourceModal");
const resourceModalLabel = document.getElementById("resourceModalLabel");

const resourceModal = new bootstrap.Modal(resourceModalEl);

let editingResourceId = null;
let loadedResources = [];

if (!token || role !== "ADMIN") {
  window.location.href = "index.html";
}

function authHeaders() {
  return {
    Authorization: "Bearer " + token,
    "Content-Type": "application/json"
  };
}

function showPageAlert(message, type = "danger") {
  pageAlert.textContent = message;
  pageAlert.className = `alert alert-${type}`;
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

function escapeHtml(value) {
  const div = document.createElement("div");
  div.textContent = value ?? "";
  return div.innerHTML;
}

// ========================================
// Resource loading and filtering
// ========================================
/**
 * Loads active resources from the backend and refreshes the table.
 */
async function loadResources() {
  hidePageAlert();

  resourcesTableBody.innerHTML = `
    <tr>
      <td colspan="7" class="text-muted">
        Loading resources...
      </td>
    </tr>
  `;

  try {
    const response = await fetch(API_BASE, {
      headers: authHeaders()
    });

    if (!response.ok) {
      throw new Error(await response.text());
    }

    loadedResources = await response.json();

    applyFilters();

  } catch (error) {
    console.error(error);

    resourcesTableBody.innerHTML = `
      <tr>
        <td colspan="7" class="text-danger">
          Failed to load resources.
        </td>
      </tr>
    `;

    showPageAlert(
      "Could not load resources. Check that you are logged in as an admin and the backend is running."
    );
  }
}

/**
 * Filters loaded resources by leadership language and resource type.
 */

function applyFilters() {
  const selectedLanguage = filterLanguage.value;
  const selectedType = filterType.value;

  const filtered = loadedResources.filter(resource => {
    const languageMatches =
      !selectedLanguage ||
      resource.leadershipLanguage === selectedLanguage;

    const typeMatches =
      !selectedType ||
      resource.resourceType === selectedType;

    return languageMatches && typeMatches;
  });

  renderResources(filtered);
}

/**
 * Securely retrieves and opens a stored resource using the current JWT.
 *
 * @param {number} id resource identifier
 */

async function viewResource(id) {
  try {
    const response = await fetch(
      `${API_BASE}/${id}/file`,
      {
        headers: {
          Authorization: "Bearer " + token
        }
      }
    );

    if (!response.ok) {
      throw new Error(await response.text());
    }

    const blob = await response.blob();
    const fileUrl = URL.createObjectURL(blob);

    window.open(fileUrl, "_blank");

    setTimeout(() => {
      URL.revokeObjectURL(fileUrl);
    }, 60000);

  } catch (error) {
    console.error(error);
    showPageAlert("Could not open resource.");
  }
}

/**
 * Renders resources into the admin management table.
 *
 * @param {Array} resources resources to display
 */

function renderResources(resources) {
  resourceCount.textContent =
    `${resources.length} resource${resources.length === 1 ? "" : "s"}`;

  if (resources.length === 0) {
    resourcesTableBody.innerHTML = `
      <tr>
        <td colspan="7" class="text-muted">
          No resources found.
        </td>
      </tr>
    `;
    return;
  }

  resourcesTableBody.innerHTML = resources
    .map(resource => `
      <tr>
        <td>${resource.displayOrder ?? ""}</td>

        <td>
          ${escapeHtml(resource.leadershipLanguage || "UNCLASSIFIED")}
        </td>

        <td>
          ${escapeHtml(resource.resourceType || "")}
        </td>

        <td>
          ${escapeHtml(resource.title || "")}
        </td>

        <td>
          ${escapeHtml(resource.originalFileName || "External link")}
        </td>

        <td>
          <span class="badge ${resource.active ? "bg-success" : "bg-secondary"}">
            ${resource.active ? "Yes" : "No"}
          </span>
        </td>

        <td class="text-end text-nowrap">

          ${
            resource.storageKey
              ? `
                <button
                    type="button"
                    class="btn btn-sm btn-outline-secondary me-1"
                    data-action="view"
                    data-id="${resource.id}"
                >
                  View
                </button>
              `
              : ""
          }

          <button
            type="button"
            class="btn btn-sm btn-outline-primary me-1"
            data-action="edit"
            data-id="${resource.id}"
          >
            Edit
          </button>

          <button
            type="button"
            class="btn btn-sm btn-outline-danger"
            data-action="delete"
            data-id="${resource.id}"
          >
            Delete
          </button>

        </td>
      </tr>
    `)
    .join("");
}

// ========================================
// Resource creation and editing
// ========================================

function resetForm() {
  editingResourceId = null;

  resourceForm.reset();

  document.getElementById("resourceLeadershipLanguage").value = "";
  document.getElementById("resourceType").value = "";
  document.getElementById("resourceDisplayOrder").value = "0";
  document.getElementById("resourceActive").checked = true;
  document.getElementById("resourceFile").value = "";

  hideFormAlert();
}

function openCreateModal() {
  resetForm();

  resourceModalLabel.textContent = "Add resource";

  resourceModal.show();
}

/**
 * Loads a resource and populates the modal for metadata editing.
 *
 * @param {number} id resource identifier
 */

async function openEditModal(id) {
  hideFormAlert();

  try {
    const response = await fetch(`${API_BASE}/${id}`, {
      headers: authHeaders()
    });

    if (!response.ok) {
      throw new Error(await response.text());
    }

    const resource = await response.json();

    editingResourceId = resource.id;

    resourceModalLabel.textContent = "Edit resource";

    document.getElementById("resourceLeadershipLanguage").value =
      resource.leadershipLanguage || "UNCLASSIFIED";

    document.getElementById("resourceType").value =
      resource.resourceType || "";

    document.getElementById("resourceTitle").value =
      resource.title || "";

    document.getElementById("resourceDescription").value =
      resource.description || "";

    document.getElementById("resourceDisplayOrder").value =
      resource.displayOrder ?? 0;

    document.getElementById("resourceActive").checked =
      resource.active !== false;

    document.getElementById("resourceFile").value = "";

    resourceModal.show();

  } catch (error) {
    console.error(error);
    showPageAlert("Could not load the selected resource.");
  }
}

/**
 * Creates a new uploaded resource or updates an existing resource.
 *
 * New resources use multipart form data for file upload, while edits
 * update metadata without replacing the existing physical file.
 *
 * @param {SubmitEvent} event form submission event
 */

async function saveResource(event) {
    event.preventDefault();

    hideFormAlert();

    const title =
        document.getElementById("resourceTitle").value.trim();

    const description =
        document.getElementById("resourceDescription").value.trim();

    const leadershipLanguage =
        document.getElementById("resourceLeadershipLanguage").value;

    const resourceType =
        document.getElementById("resourceType").value;

    const active =
        document.getElementById("resourceActive").checked;

    const displayOrder =
        Number(document.getElementById("resourceDisplayOrder").value);

    const fileInput =
        document.getElementById("resourceFile");

    try {

        // CREATE NEW RESOURCE WITH FILE UPLOAD
        if (!editingResourceId) {

        if (!fileInput.files || fileInput.files.length === 0) {
            showFormAlert("Please choose a file.");
            return;
        }

        const formData = new FormData();

        formData.append("file", fileInput.files[0]);
        formData.append("title", title);
        formData.append("description", description);
        formData.append("leadershipLanguage", leadershipLanguage);
        formData.append("resourceType", resourceType);
        formData.append("displayOrder", displayOrder);
        formData.append("active", active);

        const response = await fetch(
            `${API_BASE}/upload`,
            {
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },
            body: formData
            }
        );

        if (!response.ok) {
            throw new Error(await response.text());
        }

        resourceModal.hide();

        showPageAlert(
            "Resource uploaded successfully.",
            "success"
        );

        await loadResources();

        return;
        }

        // EDIT EXISTING RESOURCE METADATA
        const payload = {
        title,
        description,
        leadershipLanguage,
        resourceType,
        resourceUrl:   loadedResources.find(resource => resource.id === editingResourceId)?.resourceUrl ?? null,
        active,
        displayOrder
        };

        const response = await fetch(
        `${API_BASE}/${editingResourceId}`,
        {
            method: "PUT",
            headers: authHeaders(),
            body: JSON.stringify(payload)
        }
        );

        if (!response.ok) {
        throw new Error(await response.text());
        }

        resourceModal.hide();

        showPageAlert(
        "Resource updated successfully.",
        "success"
        );

        await loadResources();

    } catch (error) {
        console.error(error);

        showFormAlert(
        editingResourceId
            ? "Could not update resource."
            : "Could not upload resource."
        );
    }   
}

// ========================================
// Resource deletion and UI events
// ========================================

/**
 * Deletes a resource record after administrator confirmation.
 *
 * @param {number} id resource identifier
 */
async function deleteResource(id) {
  const resource =
    loadedResources.find(item => item.id === id);

  const confirmed = window.confirm(
    `Delete "${resource?.title || "this resource"}"?`
  );

  if (!confirmed) {
    return;
  }

  try {
    const response = await fetch(
      `${API_BASE}/${id}`,
      {
        method: "DELETE",
        headers: {
          Authorization: "Bearer " + token
        }
      }
    );

    if (!response.ok) {
      throw new Error(await response.text());
    }

    showPageAlert(
      "Resource deleted successfully.",
      "success"
    );

    await loadResources();

  } catch (error) {
    console.error(error);

    showPageAlert(
      "Could not delete resource."
    );
  }
}

resourcesTableBody.addEventListener(
  "click",
  event => {
    const button =
      event.target.closest("button[data-action]");

    if (!button) {
      return;
    }

    const id = Number(button.dataset.id);

    if (button.dataset.action === "view") {
        viewResource(id);
    }

    if (button.dataset.action === "edit") {
      openEditModal(id);
    }

    if (button.dataset.action === "delete") {
      deleteResource(id);
    }
  }
);

filterLanguage.addEventListener(
  "change",
  applyFilters
);

filterType.addEventListener(
  "change",
  applyFilters
);

refreshBtn.addEventListener(
  "click",
  loadResources
);

addResourceBtn.addEventListener(
  "click",
  openCreateModal
);

resourceForm.addEventListener(
  "submit",
  saveResource
);

logoutBtn.addEventListener(
  "click",
  () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");

    window.location.href = "index.html";
  }
);

loadResources();