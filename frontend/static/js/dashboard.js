const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

const tokenStatus = document.getElementById("tokenStatus");
const userRole = document.getElementById("userRole");
const logoutBtn = document.getElementById("logoutBtn");
const backendResponse = document.getElementById("backendResponse");
const surveyBtn = document.getElementById("surveyBtn");
const adminSection = document.getElementById("adminSection");

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

  if (role === "ADMIN" && adminSection) {
    adminSection.classList.remove("d-none");
  }

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

// the wheel

const leadershipWheelCanvas = document.getElementById("leadershipWheel");

if (leadershipWheelCanvas) {

  const segments = [
    { name: "Words of Recognition", desc: "Verbal affirmation and praise" },
    { name: "Caring Time",          desc: "Intentional presence with your team" },
    { name: "Acts of Support",      desc: "Remove blockers, enable great work" },
    { name: "Psychological Touch",  desc: "Emotional safety and connection" },
    { name: "Receiving Value",      desc: "Recognising others' contributions" },
  ];

  const wheel = new Chart(leadershipWheelCanvas, {
    type: "doughnut",
    data: {
      labels: segments.map(s => s.name),
      datasets: [{
        data: [1, 1, 1, 1, 1],
        backgroundColor: ["#64BC28", "#4A9E1A", "#96DA5F", "#2E7D0E", "#B8ED85"],
        borderColor: "#013664",
        borderWidth: 3,
        hoverOffset: 14,
      }]
    },
    options: {
      layout: {
        padding: 20
      },
      plugins: {
        legend: { display: false },
        tooltip: { enabled: false },
      },
      onHover: (event, elements) => {
        const wheelName = document.getElementById("wheelName");
        const wheelDesc = document.getElementById("wheelDesc");
        if (elements.length > 0) {
          const i = elements[0].index;
          wheelName.textContent = segments[i].name;
          wheelDesc.textContent = segments[i].desc;
        } else {
          wheelName.textContent = "Hover a segment";
          wheelDesc.textContent = "";
        }
      }
    }
  });
}

function toggleNav() {
  document.getElementById('navLinks').classList.toggle('open');
  document.getElementById('hamburgerBtn').classList.toggle('open');
}

// progress over time + peer comparison 

const API_BASE = "http://localhost:8080";

const CATEGORIES = [
  { key: "caringTime", label: "Caring time", color: "#64BC28", dash: [], point: "circle" },
  { key: "psychologicalTouch", label: "Psych. touch", color: "#00284B", dash: [8, 3, 2, 3], point: "rectRot" },
  { key: "actsOfSupport", label: "Acts of support", color: "#2a78d6", dash: [6, 4], point: "triangle" },
  { key: "receivingValue", label: "Receiving value", color: "#e87ba4", dash: [10, 3], point: "rect" },
  { key: "wordsOfRecognition", label: "Words of recognition", color: "#eb6834", dash: [2, 2], point: "star" },
];

function capitalize(key) {
  return key.charAt(0).toUpperCase() + key.slice(1);
}

function bandForScore(score) {
  if (score >= 190) return "Strong intent";
  if (score >= 150) return "Building momentum";
  return "Getting started";
}

function ordinal(n) {
  const s = ["th", "st", "nd", "rd"], v = n % 100;
  return n + (s[(v - 20) % 10] || s[v] || s[0]);
}

async function loadProgressOverTime() {
  try {
    const res = await fetch(API_BASE + "/api/dashboard/user", {
      method: "GET",
      headers: { "Authorization": "Bearer " + token }
    });
    const entries = await res.json(); // oldest -> newest
    if (!entries || !entries.length) return;

   
    const recent = entries.slice(-5);
    const weekLabels = recent.map((_, i) => "Week " + (i + 1));
    const overallScores = recent.map(e => e.overallScore);

    const latest = recent[recent.length - 1];
    const previous = recent.length > 1 ? recent[recent.length - 2] : null;

    document.getElementById("progressSubtitle").textContent =
      recent.length + " of 5 weekly submissions so far";
    document.getElementById("progressLatestScore").innerHTML =
      latest.overallScore + "<small> /250</small>";
    document.getElementById("progressBand").textContent =
      latest.scoreBand || bandForScore(latest.overallScore);

    const deltaLabelEl = document.getElementById("progressDeltaLabel");
    const deltaEl = document.getElementById("progressDelta");
    if (previous) {
      const delta = latest.overallScore - previous.overallScore;
      deltaLabelEl.textContent = "Since week " + (recent.length - 1);
      deltaEl.textContent = (delta >= 0 ? "+" : "") + delta;
      deltaEl.style.color = delta >= 0 ? "#3B6D11" : "#00284B";
    } else {
      deltaLabelEl.textContent = "Since last survey";
      deltaEl.textContent = "—";
    }

    new Chart(document.getElementById("overallChart"), {
      type: "line",
      data: {
        labels: weekLabels,
        datasets: [{
          data: overallScores,
          borderColor: "#64BC28",
          backgroundColor: "rgba(100,188,40,0.12)",
          borderWidth: 2,
          fill: true,
          tension: 0.35,
          pointRadius: overallScores.map((_, i) => i === overallScores.length - 1 ? 7 : 3),
          pointHoverRadius: 8,
          pointBackgroundColor: "#64BC28",
          pointBorderColor: "#ffffff",
          pointBorderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (ctx) => {
                const e = recent[ctx.dataIndex];
                return "Score " + ctx.parsed.y + " — " + (e.scoreBand || bandForScore(ctx.parsed.y));
              }
            }
          }
        },
        scales: {
          y: { min: 0, max: 250, grid: { color: "#E5E8E8" }, ticks: { color: "rgba(0,40,75,0.5)", font: { size: 11 } } },
          x: { grid: { display: false }, ticks: { color: "rgba(0,40,75,0.5)", font: { size: 11 } } }
        }
      }
    });

    new Chart(document.getElementById("languageChart"), {
      type: "line",
      data: {
        labels: weekLabels,
        datasets: CATEGORIES.map(c => ({
          label: c.label,
          data: recent.map(e => e[c.key + "Score"]),
          borderColor: c.color,
          borderWidth: 2,
          borderDash: c.dash,
          pointStyle: c.point,
          pointRadius: 3,
          tension: 0.3
        }))
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: { min: 0, max: 50, grid: { color: "#E5E8E8" }, ticks: { color: "rgba(0,40,75,0.5)", font: { size: 11 } } },
          x: { grid: { display: false }, ticks: { color: "rgba(0,40,75,0.5)", font: { size: 11 } } }
        }
      }
    });

    CATEGORIES.forEach(c => {
      const el = document.getElementById("legend" + capitalize(c.key));
      if (el) el.textContent = latest[c.key + "Score"];
    });
  } catch (err) {
    console.error("Could not load progress over time", err);
  }
}

async function loadPeerComparison() {
  try {
    const res = await fetch(API_BASE + "/api/dashboard/peer-comparison", {
      method: "GET",
      headers: { "Authorization": "Bearer " + token }
    });
    const data = await res.json();

   
    const rows = CATEGORIES
      .map(c => ({
        label: c.label,
        score: data["your" + capitalize(c.key) + "Score"],
        percentile: data[c.key + "Percentile"]
      }))
      .filter(r => typeof r.percentile === "number")
      .sort((a, b) => b.percentile - a.percentile);

    if (!rows.length) return;


    const avgPercentile = Math.round(
      rows.reduce((sum, r) => sum + r.percentile, 0) / rows.length
    );
    document.getElementById("peerOverallPercentile").textContent = ordinal(avgPercentile);

    const container = document.getElementById("peerRows");
    container.innerHTML = "";
    rows.forEach(r => {
      const row = document.createElement("div");
      row.className = "peer-row";
      row.innerHTML =
        '<div class="peer-row-top">' +
          '<span class="peer-row-name">' + r.label + '</span>' +
          '<span class="peer-row-pct">' + ordinal(r.percentile) + ' percentile</span>' +
        '</div>' +
        '<div class="peer-track">' +
          '<div class="peer-fill" style="width:' + r.percentile + '%;"></div>' +
        '</div>';
      container.appendChild(row);
    });
  } catch (err) {
    console.error("Could not load peer comparison", err);
  }
}

if (token && role !== "ADMIN") {
  loadProgressOverTime();
  loadPeerComparison();
}