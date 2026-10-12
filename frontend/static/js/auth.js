const API_ROOT =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1"
        ? "http://localhost:8080"
        : "https://leadership-compass-api.ashysand-21bb09f6.australiaeast.azurecontainerapps.io";

const BASE_URL = `${API_ROOT}/api/auth`;
const API_URL = `${API_ROOT}/api`;

function finishLogin(data, isAdminLogin, message) {
    if (isAdminLogin && !["ADMIN", "TGG_ADMIN"].includes(data.role)) {
        message.textContent = "This account does not have admin access.";
        message.className = "mt-3 text-center text-danger";
        return;
    }

    localStorage.setItem("token", data.token);
    localStorage.setItem("role", data.role || "USER");

    message.textContent = "Login successful!";
    message.className = "mt-3 text-center text-success";

    setTimeout(() => {
        if (data.role === "TGG_ADMIN") {
            window.location.href = "admin.html";
        } else if (data.role === "USER") {
            window.location.href = "dashboard.html";
        } else {
            localStorage.removeItem("token");
            localStorage.removeItem("role");
            message.textContent = "This account no longer has dashboard access.";
            message.className = "mt-3 text-center text-danger";
        }
    }, 800);
}

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
                finishLogin(data, isAdminLogin, message);
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

const forgotPasswordForm = document.getElementById("forgotPasswordForm");

if (forgotPasswordForm) {
    forgotPasswordForm.addEventListener("submit", async function (event) {
        event.preventDefault();
        const email = document.getElementById("forgotEmail").value.trim();
        const message = document.getElementById("forgotMessage");
        if (!email) {
            message.textContent = "Enter the email on your account.";
            message.className = "mt-3 text-center text-danger";
            return;
        }
        try {
            const response = await fetch(`${BASE_URL}/forgot-password`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email: email })
            });
            const data = await response.json();
            message.textContent = data.message || "If an account exists for that email, a reset link has been sent.";
            message.className = response.ok
                ? "mt-3 text-center text-success"
                : "mt-3 text-center text-danger";
        } catch (error) {
            message.textContent = "Cannot connect to backend.";
            message.className = "mt-3 text-center text-danger";
        }
    });
}

const resetPasswordForm = document.getElementById("resetPasswordForm");

if (resetPasswordForm) {
    const token = new URLSearchParams(window.location.search).get("token");
    const message = document.getElementById("resetMessage");
    if (!token) {
        message.textContent = "This reset link is invalid or has expired.";
        message.className = "mt-3 text-center text-danger";
    }

    resetPasswordForm.addEventListener("submit", async function (event) {
        event.preventDefault();
        const password = document.getElementById("resetPassword").value;
        const confirmPassword = document.getElementById("resetConfirmPassword").value;
        if (!token) {
            message.textContent = "This reset link is invalid or has expired.";
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
        try {
            const response = await fetch(`${BASE_URL}/reset-password`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    token: token,
                    password: password
                })
            });
            const data = await response.json();
            message.textContent = data.message || "Password could not be reset.";
            message.className = response.ok
                ? "mt-3 text-center text-success"
                : "mt-3 text-center text-danger";
            if (response.ok) {
                setTimeout(() => {
                    window.location.href = "login.html";
                }, 1200);
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
    // Public registration is USER-only; the Admin dropdown is disabled.
    const role = "USER";
    const department = document.getElementById("registerDepartment").value;
    const organisation = document.getElementById("registerOrganisation").value.trim();
    const message = document.getElementById("registerMessage");
    const codeGroup = document.getElementById("signupCodeGroup");

    try {
        if (codeGroup && !codeGroup.hidden) {
            const code = document.getElementById("signupCode").value.trim();
            if (!/^\d{6}$/.test(code)) {
                message.textContent = "Enter the 6-digit code from your email.";
                message.className = "mt-3 text-center text-danger";
                return;
            }
            const verifyResponse = await fetch(`${BASE_URL}/verify-signup`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email,
                    code: code
                })
            });
            const verifyData = await verifyResponse.json();
            if (verifyResponse.ok && !verifyData.twoFactorRequired) {
                message.textContent = verifyData.message || "Email confirmed. Redirecting to sign in...";
                message.className = "mt-3 text-center text-success";
                setTimeout(() => {
                    window.location.href = "login.html";
                }, 1000);
            } else {
                message.textContent = verifyData.message || "That code is invalid or has expired.";
                message.className = "mt-3 text-center text-danger";
            }
            return;
        }

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

      if (response.ok && data.twoFactorRequired) {
        codeGroup.hidden = false;
        const signupButton = document.getElementById("signupButton");
        if (signupButton) signupButton.textContent = "Confirm email";
        message.textContent = data.message || "Enter the code sent to your email.";
        message.className = "mt-3 text-center text-success";
        document.getElementById("signupCode")?.focus();
        return;
      }

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

if (dashboardLink) {
    const role = localStorage.getItem("role");

    if (role === "TGG_ADMIN") {
        dashboardLink.href = "admin.html";
    } else if (role === "USER") {
        dashboardLink.href = "dashboard.html";
    } else {
        // Do not provide a dashboard link for retired roles.
        dashboardLink.removeAttribute("href");
    }
}


