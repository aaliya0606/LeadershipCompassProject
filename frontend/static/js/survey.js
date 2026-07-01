// DOM Elements
const welcomeContainer = document.getElementById("surveyWelcomeContainer");
const questionContainer = document.getElementById("surveyQuestionContainer");
const resultsContainer = document.getElementById("surveyResultsContainer"); 

const startButton = document.getElementById("startBtn");

const items = document.querySelectorAll('.nav-item');
const panels = document.querySelectorAll('.content-panel');

// TEMPORARY JS FOR DEMO PURPOSES
// THIS WILL BE UPDATED TO CONNECT WITH BACKEND
document.addEventListener("DOMContentLoaded", () => {
    startButton.addEventListener("click", startSurvey);
});

document.querySelectorAll(".rating-btn").forEach(btn => {
    const val = parseInt(btn.dataset.value);
    btn.onclick = () => selectRating();
  });

async function startSurvey() {
  welcomeContainer.style.display = "none"; 
  questionContainer.style.display = "flex"; 
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
          ${currentPart === categories.length ? "Submit Survey" : "Next"}
        </button>
      </div>
    </div>
  `;

  surveyContainer.innerHTML = html;
  progressWrapper.style.display = "block";
}

// THIS WILL BE UPDATED TO FETCH ADVICE BASED ON REAL SURVEY RESULTS
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
