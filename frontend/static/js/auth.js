const BASE_URL = "http://localhost:8080/api/auth";

// LOGIN
const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("loginEmail").value;
        const password = document.getElementById("loginPassword").value;
        const message = document.getElementById("loginMessage");

        // Detect if it is ADMIN page
        const isAdminLogin= window.location.pathname.includes("adminLogin");

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
                    // Redirect admins to a different page
                    window.location.href = isAdminLogin ? "admin-dashboard.html" : "dashboard.html"; }, 800);
            } else {
                message.textContent = "Login successful!";
                message.className = "mt-3 text-center text-success";
            }
        } catch (error) {
            message.textContent = "Cannot connect to backend.";
            message.className = "mt-3 text-center text danger";
        }
    });
}

// REGISTER
const registerForm = document.getElementById("registerForm");

if (registerForm) {
  registerForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    //const fullName = document.getElementById("registerFullName").value;

    const firstName = document.getElementById("registerFirstName").value;
    const lastName = document.getElementById("registerLastName").value;
    const fullName = `${firstName} ${lastName}`.trim();

    const email = document.getElementById("registerEmail").value;
    const password = document.getElementById("registerPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const role = document.getElementById("registerRole").value;
    const message = document.getElementById("registerMessage");

    try {

        if (!fullName || !email || !password || !confirmPassword) {
            message.textContent = "Please fill in all fields.";
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

      const response = await fetch(`${BASE_URL}/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          fullName: fullName,
          email: email,
          password: password,
          role: role
        })
      });

      const data = await response.json();

      if (response.ok) {
        message.textContent = "Registration successful. Redirecting to login...";
        message.style.color = "red";
        message.style.fontWeight = "bold";
        message.style.fontSize = "20px";
        message.className = "mt-3 text-center text-success";

        setTimeout(() => {
          window.location.href = "login.html";
        }, 1000);
      } else {
        message.textContent = data.message || "Registration failed.";
        message.style.color = "red";
        message.style.fontWeight = "bold";
        message.style.fontSize = "20px";
        message.className = "mt-3 text-center text-danger";
      }
    } catch (error) {
      message.textContent = "Cannot connect to backend.";
      message.style.color = "red";
      message.style.fontWeight = "bold";
      message.style.fontSize = "20px";
      message.className = "mt-3 text-center text-danger";
    }
  });
}