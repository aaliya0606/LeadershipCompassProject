const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

const tokenStatus = document.getElementById("tokenStatus");
const userRole = document.getElementById("userRole");
const logoutBtn = document.getElementById("logoutBtn");
const backendResponse = document.getElementById("backendResponse");
const surveyBtn = document.getElementById("surveyBtn");

if (surveyBtn) {
  surveyBtn.addEventListener("click", function() {
    window.location.href = "survey.html";

  });
}

if (!token) {
  window.location.href = "index.html";
} else {
  tokenStatus.textContent = "JWT token found: " + token.substring(0, 40) + "...";
  userRole.textContent = role || "USER";

  let dashboardUrl = "http://localhost:8080/api/dashboard/user";

  if (role === "ADMIN") {
    dashboardUrl = "http://localhost:8080/api/dashboard/admin";
  }

  fetch(dashboardUrl, {
    method: "GET",
    headers: {
      "Authorization": "Bearer " + token
    }
  })
    .then(response => response.text())
    .then(data => {
      backendResponse.textContent = data;
    })
    .catch(error => {
      backendResponse.textContent = "Unable to connect to protected backend endpoint.";
      console.error("Dashboard error:", error);
    });
}

logoutBtn.addEventListener("click", function () {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  window.location.href = "index.html";
});