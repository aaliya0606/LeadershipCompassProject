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

async function selectRating() {
  questionContainer.style.display = "none"; 
  resultsContainer.style.display = "flex"; 
  document.getElementById('progressFill').style.width = '100%';
  document.getElementById('progressLabel').textContent = 'Complete';
  document.getElementById('progressCount').textContent = '5 of 5 parts';
  initSidebarNav(); 
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
