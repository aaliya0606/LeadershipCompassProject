const welcomeContainer = document.getElementById("surveyWelcomeContainer");
const questionContainer = document.getElementById("surveyQuestionContainer");

const startButton = document.getElementById("startBtn");


// Init Dummy Survey
document.addEventListener("DOMContentLoaded", () => {

    startButton.addEventListener("click", startSurvey);
});

async function startSurvey() {
    welcomeContainer.style.display = "none"; 
    questionContainer.style.display = "flex"; 
}