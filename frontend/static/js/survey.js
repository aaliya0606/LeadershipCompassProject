const token = localStorage.getItem("token");
const BASE_URL = "http://localhost:8080";

// Redirect to login if no token
if (!token) {
  window.location.href = "index.html";
}

// -------------------------------------------------------------------------
// State
// -------------------------------------------------------------------------
let questions = [];       // all questions loaded from backend
let answers = {};         // { "CT_1": 3, "RV_2": 4, ... }
let currentPart = 0;      // 0 = intro, 1-5 = categories, 6 = results

const categories = [
  { prefix: "CT", label: "Caring Time" },
  { prefix: "RV", label: "Receiving Value" },
  { prefix: "AS", label: "Acts of Support" },
  { prefix: "WR", label: "Words of Recognition" },
  { prefix: "PT", label: "Psychological Touch" }
];

// -------------------------------------------------------------------------
// DOM references
// -------------------------------------------------------------------------
const surveyContainer = document.querySelector(".survey-container");
const progressWrapper = document.getElementById("surveyProgressWrapper");
const progressLabel   = document.getElementById("progressLabel");
const progressCount   = document.getElementById("progressCount");
const progressFill    = document.getElementById("progressFill");
const progressTrack   = document.getElementById("progressTrack");
const getStartedBtn   = document.querySelector(".survey-button");
const navBurgerBtn    = document.getElementById('hamburgerBtn');

// -------------------------------------------------------------------------
// Initialise Navigation Bar
// -------------------------------------------------------------------------
async function initSidebarNav() {
  items.forEach(function (item) {
    item.addEventListener('click', function () {
      items.forEach(function (i) { i.classList.remove('active'); });
      panels.forEach(function (p) { p.classList.remove('active'); });

      item.classList.add('active');

      var panelId = 'panel-' + item.getAttribute('data-panel');
      var panel = document.getElementById(panelId);
      if (panel) panel.classList.add('active');
    });
  });
}

// Hamburger Button
if (navBurgerBtn) {
  navBurgerBtn.addEventListener('click', function () {
    const navLinks = document.getElementById('navLinks');
    if (navLinks) {
      navLinks.classList.toggle('open');
    }
    this.classList.toggle('open');
  });
}

// -------------------------------------------------------------------------
// Load questions from backend on page load
// -------------------------------------------------------------------------
fetch(BASE_URL + "/api/survey/questions", {
  method: "GET",
  headers: { "Authorization": "Bearer " + token }
})
  .then(response => response.json())
  .then(data => {
    questions = data;
    // Attach Get Started button now that questions are loaded
    if (getStartedBtn) {
      getStartedBtn.addEventListener("click", function () {
        currentPart = 1;
        renderPart();
      });
    }
  })
  .catch(error => {
    console.error("Failed to load questions:", error);
    showError("Could not load survey questions. Please try again later.");
  });

// -------------------------------------------------------------------------
// Update progress bar
// -------------------------------------------------------------------------
function updateProgress() {
  const percent = Math.round((currentPart / categories.length) * 100);
  progressFill.style.width = percent + "%";
  progressTrack.setAttribute("aria-valuenow", percent);

  if (currentPart === 0) {
    progressLabel.textContent = "Introduction";
    progressCount.textContent = "0 of 5 parts";
  } else if (currentPart <= categories.length) {
    progressLabel.textContent = categories[currentPart - 1].label;
    progressCount.textContent = currentPart + " of 5 parts";
  } else {
    progressLabel.textContent = "Complete";
    progressCount.textContent = "5 of 5 parts";
  }
}

// -------------------------------------------------------------------------
// Render current category part
// -------------------------------------------------------------------------
function renderPart() {
    window.scrollTo(0, 0);
  updateProgress();

  if (currentPart > categories.length) {
    submitSurvey();
    return;
  }

  const category = categories[currentPart - 1];
  const categoryQuestions = questions.filter(q => q.category === category.prefix);

  let html = `
    <div class="survey-category">
      <h2 class="survey-category-title">${category.label}</h2>
      <p class="survey-category-subtitle">Rate yourself from 1 (Rarely) to 5 (Consistently)</p>
      <div class="survey-questions">
  `;

  categoryQuestions.forEach((q, index) => {
    const questionKey = category.prefix + "_" + q.questionId;
    html += `
      <div class="survey-question" id="q-${questionKey}">
        <p class="survey-question-text">${index + 1}. ${q.questionText}</p>
        <div class="survey-options">
          ${[1, 2, 3, 4, 5].map(score => `
            <label class="survey-option">
              <input type="radio" name="${questionKey}" value="${score}" 
                ${answers[questionKey] === score ? "checked" : ""} />
              <span class="survey-option-label">${score}</span>
            </label>
          `).join("")}
        </div>
        <div class="survey-scale-labels">
          <span>Rarely</span>
          <span>Consistently</span>
        </div>
      </div>
    `;
  });

  html += `
      </div>
      <div class="survey-nav">
        ${currentPart > 1 ? `<button class="survey-btn-back" onclick="goBack()">Back</button>` : ""}
        <button class="survey-btn-next" onclick="goNext('${category.prefix}')">
          ${currentPart === categories.length ? "Submit" : "Next"}
        </button>
      </div>
    </div>
  `;

  surveyContainer.innerHTML = html;
  progressWrapper.style.display = "block";
}

// -------------------------------------------------------------------------
// Go to next part (save answers first)
// -------------------------------------------------------------------------
function goNext(prefix) {
  const categoryQuestions = questions.filter(q => q.category === prefix);
  let allAnswered = true;

  categoryQuestions.forEach(q => {
    const questionKey = prefix + "_" + q.questionId;
    const selected = document.querySelector(`input[name="${questionKey}"]:checked`);
    if (selected) {
      answers[questionKey] = parseInt(selected.value);
    } else {
      allAnswered = false;
      document.getElementById("q-" + questionKey).classList.add("unanswered");
    }
  });

  if (!allAnswered) {
    showError("Please answer all questions before continuing.");
    return;
  }

  currentPart++;
  renderPart();
  window.scrollTo(0, 0);
}

// -------------------------------------------------------------------------
// Go back to previous part
// -------------------------------------------------------------------------
function goBack() {
  currentPart--;
  renderPart();
  window.scrollTo(0, 0);
}

// -------------------------------------------------------------------------
// Submit survey to backend
// -------------------------------------------------------------------------
function submitSurvey() {
  updateProgress();
  surveyContainer.innerHTML = `<div class="survey-loading"><p>Submitting your survey...</p></div>`;

  fetch(BASE_URL + "/api/survey/submit", {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + token,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ answers: answers })
  })
    .then(response => {
      if (!response.ok) throw new Error("Submission failed");
      return response.json();
    })
    .then(result => {
      showResults(result);
    })
    .catch(error => {
      console.error("Submission error:", error);
      showError("Failed to submit survey. Please try again.");
    });
}

// -------------------------------------------------------------------------
// Show results
// -------------------------------------------------------------------------
function showResults(result) {
  const bandColor = {
    "High": "#28a745",
    "Strong intent": "#17a2b8",
    "Needs attention": "#ffc107",
    "Blind spot": "#dc3545"
  };

  let html = `
    <div class="survey-results">
      <h2 class="survey-results-title">Your Leadership Profile</h2>
      <div class="survey-overall">
        <p class="survey-overall-score">Overall Score: <strong>${result.overallScore} / 250</strong></p>
        <span class="survey-band" style="background:#00284B">${result.overallBand}</span>
      </div>
      <p class="survey-summary">${result.summary}</p>
      <div class="survey-category-results">
        ${renderCategoryResult("Caring Time",         result.caringTimeScore,         result.caringTimeBand,         result.caringTimeMessage,         bandColor)}
        ${renderCategoryResult("Receiving Value",     result.receivingValueScore,     result.receivingValueBand,     result.receivingValueMessage,     bandColor)}
        ${renderCategoryResult("Acts of Support",     result.actsOfSupportScore,     result.actsOfSupportBand,     result.actsOfSupportMessage,     bandColor)}
        ${renderCategoryResult("Words of Recognition",result.wordsOfRecognitionScore,result.wordsOfRecognitionBand,result.wordsOfRecognitionMessage,bandColor)}
        ${renderCategoryResult("Psychological Touch", result.psychologicalTouchScore, result.psychologicalTouchBand, result.psychologicalTouchMessage, bandColor)}
      </div>
      <button class="survey-button" onclick="window.location.href='dashboard.html'">Dashboard</button>
    </div>
  `;

  surveyContainer.innerHTML = html;
  window.scrollTo(0, 0);

  requestAnimationFrame(() => {
    document.querySelectorAll('.result-bar-fill').forEach(fill => {
      fill.style.width = fill.dataset.targetWidth;
    });
    document.querySelectorAll('.result-bar-marker').forEach(marker => {
      marker.style.left = marker.dataset.targetLeft;
    });
  });
}

function renderResultBar(label, score, max, band, color) {
  const pct = Math.max(0, Math.min(100, (score / max) * 100));
  // const fillStyle = color ? `background:${color};` : '';

  return `
  <div class="result-bar-row">
    <div class="result-bar-marker" data-target-left="${pct}%" style="left:0%; color:#00284B;">
      <span class="survey-band" style="background:#00284B;">${band}</span>
    </div>
    <div class="result-bar-track">
      <div class="result-bar-fill" data-target-width="${pct}%" style="width:0%;">${score}</div>
    </div>
    </div>
  `;
}

function renderCategoryResult(name, score, band, message, bandColor) {
  return `
    <div class="survey-category-result">
      <span class="result-bar-label" style="text-align:center;">${name}</span>
      ${renderResultBar(name, score, 50, band, bandColor[band])}
      <p class="survey-category-score">Score: <strong>${score} / 50</strong></p>
      <p class="survey-category-message">${message}</p>
    </div>
  `;
}

// -------------------------------------------------------------------------
// Show error message
// -------------------------------------------------------------------------
function showError(message) {
  const existing = document.getElementById("surveyError");
  if (existing) existing.remove();

  const error = document.createElement("div");
  error.id = "surveyError";
  error.className = "survey-error";
  error.textContent = message;
  surveyContainer.appendChild(error);

  setTimeout(() => error.remove(), 4000);
}