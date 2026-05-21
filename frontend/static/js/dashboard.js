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

// leadership wheel
const segments = [
  { name: "Words of Recognition", desc: "Verbal affirmation and praise" },
  { name: "Caring Time",          desc: "Intentional presence with your team" },
  { name: "Acts of Support",      desc: "Remove blockers, enable great work" },
  { name: "Psychological Touch",  desc: "Emotional safety and connection" },
  { name: "Receiving Value",      desc: "Recognising others' contributions" },
];

const wheel = new Chart(document.getElementById("leadershipWheel"), {
  type: "doughnut",
  data: {
    labels: segments.map(s => s.name),
    datasets: [{
      data: [1, 1, 1, 1, 1],
      backgroundColor: ["#64BC28", "#4A9E1A", "#96DA5F", "#2E7D0E", "#B8ED85"],
      borderColor: "#013664",
      borderWidth: 3,
      hoverOffset: 18,
    }]
  },
  options: {
    plugins: {
      legend: { display: false },
      tooltip: { enabled: false },
    },
    onHover: (event, elements) => {
      if (elements.length > 0) {
        const i = elements[0].index;
        document.getElementById("wheelName").textContent = segments[i].name;
        document.getElementById("wheelDesc").textContent = segments[i].desc;
      } else {
        document.getElementById("wheelName").textContent = "Hover a segment";
        document.getElementById("wheelDesc").textContent = "";
      }
    }
  }
});




