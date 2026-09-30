const API_BASE_URL =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1"
        ? "http://localhost:8080"
        : "https://leadership-compass-api.ashysand-21bb09f6.australiaeast.azurecontainerapps.io";

const token =
    localStorage.getItem("token");

const role =
    localStorage.getItem("role");


const logoutBtn =
    document.getElementById("logoutBtn");

const surveyBtn =
    document.getElementById("surveyBtn");

const adminSection =
    document.getElementById("adminSection");

const generate360Btn =
    document.getElementById("generate360Btn");

const no360SurveyState =
    document.getElementById("no360SurveyState");

const active360SurveyState =
    document.getElementById("active360SurveyState");

const surveyLink =
    document.getElementById("surveyLink");

const copySurveyLinkBtn =
    document.getElementById("copySurveyLinkBtn");

const copySurveyLinkSecondaryBtn =
    document.getElementById(
        "copySurveyLinkSecondaryBtn"
    );

const view360ResultsBtn =
    document.getElementById(
        "view360ResultsBtn"
    );

const surveyStatus =
    document.getElementById(
        "surveyStatus"
    );

const surveyStatusBadge =
    document.getElementById(
        "surveyStatusBadge"
    );

const responseCount =
    document.getElementById(
        "responseCount"
    );

const surveyIdDisplay =
    document.getElementById(
        "surveyIdDisplay"
    );

const surveyExpiry =
    document.getElementById(
        "surveyExpiry"
    );

const surveyHistoryContainer =
    document.getElementById(
        "surveyHistoryContainer"
    );


/*
 * Current survey information.
 *
 * This only needs to stay in memory while
 * the dashboard page is open.
 *
 * The database is now responsible for
 * remembering the user's active survey.
 */
let current360SurveyId = null;
let current360SurveyToken = null;


/* =========================================================
   AUTHENTICATION
   ========================================================= */

if (!token) {

    window.location.href =
        "index.html";

}

logoutBtn.addEventListener("click", function () {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  window.location.href = "index.html";
});

//NEW: replaced prev version, updated to work on the host 28/09/26
const API_BASE = API_BASE_URL;

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


/* =========================================================
   EMPTY STATES (NEW 30/09/26)
   Shared helpers so every "no survey yet" / error message
   uses the same .empty-state style from main.css.
   ========================================================= */

function makeEmptyState(text) {
  const el = document.createElement("div");
  el.className = "empty-state";
  el.textContent = text;
  return el;
}

function showRadarEmptyState(message) {
  const container = document.querySelector(".wheel-container");
  const subtitleEl = document.getElementById("radarSubtitle");
  if (subtitleEl) subtitleEl.textContent = "Your scores across the five languages";
  if (!container) return;
  container.classList.add("is-empty");
  if (!container.querySelector(".empty-state")) container.appendChild(makeEmptyState(message));
}

function showProgressEmptyState(message) {
  const card = document.querySelector(".progress-card");
  const subtitleEl = document.getElementById("progressSubtitle");
  if (subtitleEl) subtitleEl.textContent = "Track how your scores change across the 5 weeks.";
  if (!card) return;
  card.classList.add("is-empty");
  if (!card.querySelector(".empty-state")) card.appendChild(makeEmptyState(message));
}



async function loadLeadershipRadar() {
  const canvas = document.getElementById("leadershipRadar");
  const subtitleEl = document.getElementById("radarSubtitle");
  if (!canvas) return;

  try {
    const res = await fetch(API_BASE + "/api/dashboard/latest-scores", {
      method: "GET",
      headers: { "Authorization": "Bearer " + token }
    });

    //NEW: show empty state instead of throwing (30/09/26)
    if (!res.ok) {
      showRadarEmptyState((await userHasSurvey())
        ? "Could not load your latest scores right now."
        : "Unlocks once you have completed the survey.");
      return;
    }
    const scores = await res.json();

    const labels = CATEGORIES.map(c => c.label);
    const values = CATEGORIES.map(c => (scores ? scores[c.key + "Score"] : undefined));
    const hasScores = values.some(v => typeof v === "number");

    //NEW: empty state for users with no survey yet (30/09/26)
    if (!hasScores) {
      showRadarEmptyState("Unlocks once you have completed the survey.");
      return;
    }

    if (subtitleEl) subtitleEl.textContent = "Your most recent survey scores";

    new Chart(canvas, {
      type: "radar",
      data: {
        labels,
        datasets: [{
          label: "Score",
          data: values.map(v => v || 0),
          backgroundColor: "rgba(0, 40, 75, 0.16)",
          borderColor: "#00284B",
          borderWidth: 2,
          pointBackgroundColor: CATEGORIES.map(c => c.color),
          pointBorderColor: "#ffffff",
          pointBorderWidth: 1.5,
          pointRadius: 4,
          pointHoverRadius: 7,
        }]
      },
      options: {
        plugins: {
          legend: { display: false },
          tooltip: { enabled: false } 
        },
        scales: {
          r: {
            min: 0,
            max: 50,
            ticks: {
              stepSize: 10,
              showLabelBackdrop: false,
              font: { size: 9 },
              color: "rgba(0,40,75,0.45)"
            },
            grid: { color: "#E5E8E8" },
            angleLines: { color: "rgba(0,40,75,0.15)" },
            pointLabels: {
              font: { size: 11, family: "Montserrat, Arial, sans-serif" },
              color: "#00284B"
            }
          }
        },
        onHover: (event, elements) => {
          const nameEl = document.getElementById("radarPointName");
          const scoreEl = document.getElementById("radarPointScore");
          if (!nameEl || !scoreEl) return;
          if (elements.length > 0) {
            const i = elements[0].index;
            nameEl.textContent = labels[i];
            scoreEl.textContent = values[i] != null ? "Score: " + values[i] + " / 50" : "No score yet";
          } else {
            nameEl.textContent = "Hover a point";
            scoreEl.textContent = "";
          }
        }
      }
    });
  } catch (err) {
    console.error("Could not load leadership radar scores", err);
    showRadarEmptyState("Could not load your latest scores right now.");
  }
}

loadLeadershipRadar();

function toggleNav() {
  document.getElementById('navLinks').classList.toggle('open');
  document.getElementById('hamburgerBtn').classList.toggle('open');
}


const LANGUAGE_BLURBS = {
  "Words of Recognition": "Give genuine, specific praise. Call out what someone did well and why it mattered, in the moment.",
  "Caring Time": "Dedicate intentional time to demonstrating genuine care for your team. Build trust through consistent, present leadership.",
  "Acts of Support": "Translate care into visible action. Identify and remove blockers so your people can do their best work.",
  "Psychological Touch": "Create emotional safety so your team feels comfortable being open with you. Small, consistent gestures build lasting trust.",
  "Receiving Value": "Recognise and act on your team's contributions and ideas. Show that their input genuinely shapes decisions.",
};

// function weekLanguage(week) {

//   return week.language || week.theme || week.title || "";
// }

// function weekBlurb(week) {
  
//   return week.focusSummary || week.description || LANGUAGE_BLURBS[weekLanguage(week)] || "";
// }

//NEW: category and focus are checked first (28/09/2026)
function weekLanguage(week) {
  return week.category || week.language || week.theme || week.title || "";
}

function weekBlurb(week) {
  return week.focus || week.focusSummary || week.description || LANGUAGE_BLURBS[weekLanguage(week)] || "";
}

function buildWeekCell(weekNumber, language, coloured) {
  const cell = document.createElement("div");
  if (coloured) cell.className = "coloured";
  const h5 = document.createElement("h5");
  h5.appendChild(document.createTextNode("Week " + weekNumber));
  h5.appendChild(document.createElement("br"));
  const p = document.createElement("p");
  p.textContent = language;
  h5.appendChild(p);
  cell.appendChild(h5);
  return cell;
}

//NEW: helper when there is no developement plan to load (28/09/26)
function showNoPlanState(hasSurvey) {
  const focusTag = document.getElementById("currentFocusTag");
  const focusTitle = document.getElementById("currentFocusTitle");
  const focusDesc = document.getElementById("currentFocusDesc");
  if (focusTag) focusTag.textContent = hasSurvey ? "STEP 2 OF 2" : "STEP 1 OF 2";
  if (focusTitle) focusTitle.textContent = hasSurvey ? "GENERATE YOUR PLAN" : "COMPLETE THE SURVEY";
  if (focusDesc) {
    focusDesc.textContent = hasSurvey
      ? "Your survey is complete. Generate your 5 week development plan to see your weekly focus here."
      : "Your development plan is generated from your survey results. Complete the survey first, then your weekly focus will appear here.";
  }

  const comingUpCard = document.getElementById("comingUpCard");
  const comingUpTitle = document.getElementById("comingUpTitle");
  const comingUpDesc = document.getElementById("comingUpDesc");
  if (comingUpCard) comingUpCard.style.display = "";
  if (comingUpTitle) comingUpTitle.textContent = "YOUR PLAN";
  if (comingUpDesc) {
    comingUpDesc.textContent = hasSurvey
      ? "Generate your plan on the 5 Week Plan page."
      : "Unlocks once you have completed the survey.";
  }
  const planLink = comingUpCard ? comingUpCard.querySelector(".view-plan-btn") : null;
  if (planLink) {
    planLink.textContent = "Go to the 5 Week Plan →";
    planLink.style.display = hasSurvey ? "" : "none";
  }

  const grid = document.getElementById("weekOverviewGrid");
  if (grid) {
    grid.querySelectorAll(":scope > div").forEach(el => el.remove());
    const note = document.createElement("p");
    note.className = "empty-state"; //NEW: shared empty state style (30/09/26)
    note.style.gridColumn = "1 / -1"; // keeps the message on one line instead of a narrow column
    note.textContent = hasSurvey
      ? "Generate your plan to see your 5 weeks here."
      : "Complete the survey to unlock your 5 weeks.";
    grid.appendChild(note);
  }
}

//NEW: helper (28/9/26)
async function userHasSurvey() {
  try {
    const res = await fetch(API_BASE + "/api/dashboard/user", {
      method: "GET",
      headers: { "Authorization": "Bearer " + token }
    });
    if (!res.ok) return false;
    const entries = await res.json();
    return Array.isArray(entries) && entries.length > 0;
  } catch (err) {
    return false;
  }
}

async function loadDevelopmentPlan() {
  try {
    const res = await fetch(API_BASE + "/api/development-plans/current", {
      method: "GET",
      headers: { "Authorization": "Bearer " + token }
    });
    //removed
    // if (!res.ok) throw new Error("Request failed: " + res.status);
    // const plan = await res.json();

    //NEW: replacement of commented out code, calls helper function when there is no development plan yet for the user (28/09/26)
    if (!res.ok) {
      showNoPlanState(await userHasSurvey());
      return;
    }
    const plan = await res.json();
    
    console.log("[dev plan] /api/development-plans/current response:", plan);

    const weeks = (plan.weeks || []).slice().sort((a, b) => a.weekNumber - b.weekNumber);
    if (!weeks.length) {
      console.warn("[dev plan] No weeks found at plan.weeks — check the logged response above for the real key name.");
      return;
    }

    
    const currentWeek = weeks.find(w => !(w.actions || []).every(a => a.completed));
    const isPlanComplete = !currentWeek;
    const activeWeek = currentWeek || weeks[weeks.length - 1];
    const activeIndex = weeks.findIndex(w => w.weekNumber === activeWeek.weekNumber);
    const comingUpWeek = isPlanComplete ? null : (weeks[activeIndex + 1] || null);

    // Current focus card
    const focusTag = document.getElementById("currentFocusTag");
    const focusTitle = document.getElementById("currentFocusTitle");
    const focusDesc = document.getElementById("currentFocusDesc");
    if (isPlanComplete) {
      if (focusTag) focusTag.textContent = "PLAN COMPLETE";
      if (focusTitle) focusTitle.textContent = "ALL 5 WEEKS";
      if (focusDesc) focusDesc.textContent = "You've completed every action across all 5 weeks. Nice work! You can review your full plan any time.";
    } else {
      if (focusTag) focusTag.textContent = "WEEK " + activeWeek.weekNumber + " • ACTIVE";
      if (focusTitle) focusTitle.textContent = weekLanguage(activeWeek).toUpperCase();
      if (focusDesc) focusDesc.textContent = weekBlurb(activeWeek);
    }

    // Coming up card
    const comingUpCard = document.getElementById("comingUpCard");
    const comingUpTitle = document.getElementById("comingUpTitle");
    const comingUpDesc = document.getElementById("comingUpDesc");
    if (comingUpWeek) {
      if (comingUpTitle) comingUpTitle.textContent = weekLanguage(comingUpWeek).toUpperCase();
      if (comingUpDesc) comingUpDesc.textContent = weekBlurb(comingUpWeek);
      if (comingUpCard) comingUpCard.style.display = "";
    } else if (comingUpCard) {
      
    //   comingUpCard.style.display = "none";
          
    // NEW: keep green widget after the 5 week developement plan has been completed by the user (28/09/26)
      if (isPlanComplete) {
        if (comingUpTitle) comingUpTitle.textContent = "COMPLETED";
        if (comingUpDesc) comingUpDesc.textContent = "You have completed all 5 weeks of your development plan.";
      } else {
        if (comingUpTitle) comingUpTitle.textContent = "FINAL WEEK";
        if (comingUpDesc) comingUpDesc.textContent = "This is the last week of your plan, so there is nothing coming up after it.";
      }
      comingUpCard.style.display = "";
    }

    // 5 Week Overview 
    const grid = document.getElementById("weekOverviewGrid");
    if (grid) {
      grid.querySelectorAll(":scope > div").forEach(el => el.remove());
      weeks.forEach(w => {
        const isColoured = !isPlanComplete && w.weekNumber >= activeWeek.weekNumber;
        grid.appendChild(buildWeekCell(w.weekNumber, weekLanguage(w), isColoured));
      });
    }
  } catch (err) {
    console.error("Could not load development plan", err);
    
  }
}

loadDevelopmentPlan();

// progress over time + peer comparison

async function loadProgressOverTime() {
  try {
    const res = await fetch(API_BASE + "/api/dashboard/user", {
      method: "GET",
      headers: { "Authorization": "Bearer " + token }
    });

    //avoid getting stuck on "Loading..." if the request fails 
    if (!res.ok) {
      showProgressEmptyState("Could not load your progress right now.");
      return;
    }

    //replaced the previous version with this: for when user hasn't completed the survey yet 
    const entries = await res.json(); 
    if (!entries || !entries.length) {
      //shows one message 
      showProgressEmptyState("Unlocks once you have completed the survey.");
      return;
    }

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

//NEW: for when user hasn't completed the survey yet (28/09/26)
function showPeerEmptyState(hasSurvey) {
  const headline = document.querySelector(".peer-headline");
  if (headline) headline.style.display = "none";
  const container = document.getElementById("peerRows");
  if (container) {
    container.innerHTML = "";
    const note = document.createElement("p");
    note.className = "empty-state"; //NEW: shared empty state style (30/09/26)
    note.textContent = hasSurvey
      ? "Could not load your peer comparison right now."
      : "Unlocks once you have completed the survey.";
    container.appendChild(note);
  }
}

async function loadPeerComparison() {
  try {
    const res = await fetch(API_BASE + "/api/dashboard/peer-comparison", {
      method: "GET",
      headers: { "Authorization": "Bearer " + token }
    });

    
    //NEW: for when user hasn't completed the survey yet (28/09/26)
    if (!res.ok) {
      showPeerEmptyState(await userHasSurvey());
      return;
    }
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

// for the resources
async function loadSuggestedResources() {
  const container = document.getElementById('resourcesList');
  const token = localStorage.getItem('token'); 

  try {
    //NEW: updated to work on the host 28/09/26
    const response = await fetch(API_BASE + '/api/dashboard/suggested-modules', {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!response.ok) throw new Error(`Request failed: ${response.status}`);

    const resources = await response.json();

    //NEW: for when user hasn't completed the survey yet (28/09/26)
    //NEW: added shared empty-state class (30/09/26)
    if (!resources || resources.length === 0) {
      const hasSurvey = await userHasSurvey();
      container.innerHTML = hasSurvey
        ? '<p class="res-loading empty-state">No recommended resources yet.</p>'
        : '<p class="res-loading empty-state">Unlocks once you have completed the survey.</p>';
      return;
    }

    container.innerHTML = resources.map(resource => `
      <a href="${resource.resourceUrl || 'resource-library.html'}" class="res-row">
        <span class="res-title">${resource.title}</span>
        <span class="res-arrow">→</span>
      </a>
    `).join('');

  } catch (err) {
    console.error('Failed to load suggested resources:', err);
    container.innerHTML = '<p class="res-loading">Could not load resources right now.</p>';
  }
}

if (token && role !== "ADMIN") {
  loadSuggestedResources();
}