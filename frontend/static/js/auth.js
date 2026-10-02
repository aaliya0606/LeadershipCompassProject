const BASE_URL = "http://localhost:8080/api/auth";
const API_URL = "http://localhost:8080/api";

// LOGIN
const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("loginEmail").value;
        const password = document.getElementById("loginPassword").value;
        const message = document.getElementById("loginMessage");

        // Detect if it is ADMIN page
        const isAdminLogin = window.location.pathname.toLowerCase().includes("adminlogin");

        try {
            const response = await fetch(`${BASE_URL}/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });

            const data = await response.json();

            if (response.ok && data.token) {
                // if page is admin and if the account has Role Admin
                if (isAdminLogin && data.role !== "ADMIN") {
                    message.textContent = "This account does not have admin access.";
                    message.className = "mt-3 text-center text-danger";
                    return;
                }

                localStorage.setItem("token", data.token);
                localStorage.setItem("role", data.role || "USER");

                message.textContent = "Login successful!";
                message.className = "mt-3 text-center text-success";

        setTimeout(() => {
          if (data.role === "ADMIN") {
            window.location.href = "admin.html";
          } else {
            window.location.href = "dashboard.html";
          }
        }, 800);
      } else {
        message.textContent = data.message || "Login failed.";
        message.className = "mt-3 text-center text-danger";
      }
    } catch (error) {
      message.textContent = "Cannot connect to backend.";
      message.className = "mt-3 text-center text-danger";
    }
  });
}

// REGISTER
const registerForm = document.getElementById("registerForm");

if (registerForm) {
  registerForm.addEventListener("submit", async function (event) {
    event.preventDefault();

        const fullNameField = document.getElementById("registerFullName");
        const firstNameField = document.getElementById("registerFirstName");
        const lastNameField = document.getElementById("registerLastName");
        const fullName = fullNameField
            ? fullNameField.value.trim()
            : `${firstNameField?.value || ""} ${lastNameField?.value || ""}`.trim();

    const email = document.getElementById("registerEmail").value;
    const password = document.getElementById("registerPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const role = document.getElementById("registerRole").value;
    const department = document.getElementById("registerDepartment").value;
    const organisation = document.getElementById("registerOrganisation").value.trim();
    const message = document.getElementById("registerMessage");

    try {

        if (!fullName || !email || !password || !confirmPassword) {
            message.textContent = "Please fill in all fields.";
            message.className = "mt-3 text-center text-danger";
            return;
        }

        if (!organisation) {
            message.textContent = "Please enter an organisation.";
            message.className = "mt-3 text-center text-danger";
            return;
        }

        if (!department) {
            message.textContent = "Please select a department.";
            message.className = "mt-3 text-center text-danger";
            return;
        }

        if (password.length < 8) {
            message.textContent = "Password must be at least 8 characters.";
            message.className = "mt-3 text-center text-danger";
            return;
        }

        if (password !== confirmPassword) {
            message.textContent = "Passwords do not match.";
            message.className = "mt-3 text-center text-danger";
            return;
        }

      const requestBody = {
        fullName: fullName,
        email: email,
        password: password,
        role: role,
        organisation: organisation,
        department: department
      };

      const response = await fetch(`${BASE_URL}/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(requestBody)
      });

      const data = await response.json();

      if (response.ok) {
        message.textContent = "Registration successful. Redirecting to login...";
        message.className = "mt-3 text-center text-success";

        setTimeout(() => {
          window.location.href = "login.html";
        }, 1000);
      } else {
        message.textContent = data.message || "Registration failed.";
        message.className = "mt-3 text-center text-danger";
      }
    } catch (error) {
      message.textContent = "Cannot connect to backend.";
      message.className = "mt-3 text-center text-danger";
    }
  });
}


// Helpers

function getToken() {
    return localStorage.getItem("token");
}

function handleSignOut() {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.href = "login.html";
}

async function apiFetch (path, options = {}) {
    const response = await fetch (`${API_URL}${path}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${getToken()}`,
            ...(options.headers || {})
        }
    });

    if (response.status === 401) {
        handleSignOut();
        throw new Error("Session expired. Please sign in again.");
    }

    let data = null;
    try {

        data = await response.json();
    } catch (e) {
        // empty - leave as it is
    }

    if (!response.ok) {
        // same error shape as login/sign-up
        throw new Error ((data && data.message) || "Request failed.");
    }
    return data;

}

let savedProfile = null;
function getNameParts(user) {
    if (user.firstName) {
        return { firstName: user.firstName, lastName: user.lastName || "" };
    }
    const parts = (user.fullName || "").trim().split(/\s+/);
    return { firstName: parts[0] || "", lastName: parts.slice(1).join(" ") };
}

function getInitials(firstName, lastName) {
    return ((firstName[0] || "") + (lastName[0] || "")).toUpperCase();
}

function showProfileMessage(text, type) {
    const el = document.getElementById("forMessage");
    if (!el) return;
    el.textContent = text;
    el.className = `mt-3 text-center ${type === "success" ? "text-success" : "text-danger"}`;
}

function showFieldErrors(errors) {
    ["username", "phone"].forEach((field) => {
        const el = document.getElementById(`${field}Error`);
        if (el) el.textContent = errors[field] || "";
    });
}

async function loadProfile() {
    if (!getToken()) {
        window.location.href = "login.html";
        return;
    }

    const user = await apiFetch("/profile");
    savedProfile = user;

    const {firstName, lastName } = getNameParts(user);
    document.getElementById("firstName").value = firstName;
    document.getElementById("lastName").value = lastName;
    document.getElementById("email").value = user.email;
    document.getElementById("username").value = user.username || "";
    document.getElementById("phone").value = user.phone || "";

    // Show organisation and department fields for all users
    const organisationField = document.getElementById("organisationField");
    const organisationInput = document.getElementById("organisation");
    const departmentField = document.getElementById("departmentField");
    const departmentInput = document.getElementById("department");

    if (organisationField && organisationInput) {
        organisationField.style.display = "block";
        organisationInput.value = user.organisation || "";
    }
    if (departmentField && departmentInput) {
        departmentField.style.display = "block";
        departmentInput.value = user.department || "";
    }

    document.getElementById("avatarInitials").textContent = getInitials(firstName, lastName);
    document.getElementById("sidebarName").textContent = `${firstName} ${lastName}`.trim();
}

function validateProfile(username, phone) {
    const errors = {};

    if (!/^[a-zA-Z0-9._-]{3,20}$/.test(username)) {
        errors.username = "3-20 characters: letters, numbers, . _ -";
    }

    // Accepts prefixes or 0, special characters allowed
    const cleaned = phone.replace(/[\s()-]/g, "");
    if (!/^(\+61|0)[2-478]\d{8}$/.test(cleaned)) {
        errors.phone = "Enter a valid Australian phone number.";
    }
    return errors;
}

async function isUsernameAvailable(username) {
    if (savedProfile && username === savedProfile.username) return true;
    const data = await apiFetch(`/users/check-username=${encodeURIComponent(username)}`);
    return data.available;
}

async function saveProfile() {
    const username = document.getElementById("username").value.trim();
    const phone = document.getElementById("phone").value.trim();

    try {
        const errors = validateProfile(username, phone);
        if(errors.username && !(await isUsernameAvailable(username))) {
            errors.username = "That username is already taken.";
        }
        if (Object.keys(errors).length > 0) {
            showFieldErrors(errors);
            return;
        }

        showFieldErrors({});
        savedProfile = await apiFetch("/profile", {
            method: "PUT",
            body: JSON.stringify({username, phone})
        });
        showProfileMessage("Profile saved.", "success");
    } catch (error) {
        showProfileMessage(error.message, "error");
    }
}

function cancelProfileChanges() {
    if (!savedProfile) return;
    document.getElementById("username").value = savedProfile.username || "";
    document.getElementById("phone").value = savedProfile.phone || "";
    showFieldErrors({});
    showProfileMessage("", "success");
}

async function changePassword(currentPassword, newPassword, confirmPassword) {
    if (!currentPassword || !newPassword || !confirmPassword) {
        return showProfileMessage("Please fill in all password fields.", "error");
    }
    if (newPassword.length < 8) {
        return showProfileMessage("Passwords do not match.", "error");
    }
    if (newPassword === currentPassword) {
        return showProfileMessage("New password do not match.", "error");
    }
    if (newPassword === currentPassword) {
        return showProfileMessage("New password must be different.", "error");
    }

    try {
        await apiFetch("/profile/password", {
            method: "PUT",
            body: JSON.stringify({ currentPassword, newPassword })
        });
        showProfileMessage("Password updated.", "success");
    } catch (error) {
        showProfileMessage(error.message, "error");
    }
}

const saveBtn = document.getElementById("saveBtn");

if (saveBtn) {
    loadProfile().catch((error) => showProfileMessage(error.message, "error"));

    saveBtn.addEventListener("click", saveProfile);
    document.getElementById("cancelBtn").addEventListener("click", cancelProfileChanges);

    const signOutBtn = document.getElementById("signOutBtn");
    if (signOutBtn) signOutBtn.addEventListener("click", handleSignOut);
}

// Organisation and department are now required for all users, no need to toggle visibility

// DASHBOARD CONFIG
const dashboardLink = document.querySelector(".dashboard-link");
if (dashboardLink){
    const role = localStorage.getItem("role");
    if (role === "ADMIN") {
        dashboardLink.href = "admin.html";
    } else {
        dashboardLink.href = "dashboard.html";
    }


}


