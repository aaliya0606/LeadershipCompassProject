const API_BASE_URL = "http://localhost:8080";

let currentLeaderName = "[Name]";
let currentSurveyId = null;
let currentToken = null;


/* =========================================================
   LOAD SURVEY DETAILS
   Gets the token from the URL and finds the leader
   ========================================================= */

async function loadSurveyDetails() {

    const urlParams = new URLSearchParams(window.location.search);

    currentToken = urlParams.get("token");

    if (!currentToken) {
        console.error("No survey token found in URL.");
        return false;
    }

    console.log("Survey token:", currentToken);

    try {

        const response = await fetch(
            `${API_BASE_URL}/api/360/surveys/${currentToken}`
        );

        if (!response.ok) {
            throw new Error(`HTTP error: ${response.status}`);
        }

        const survey = await response.json();

        console.log("Survey details:", survey);

        currentSurveyId = survey.id;

        if (survey.leaderName) {
            currentLeaderName = survey.leaderName;
        }

        /*
         * Update the leader name in the page
         */

        const leaderNameElement =
            document.getElementById("leaderName");

        if (leaderNameElement) {
            leaderNameElement.textContent = currentLeaderName;
        }

        /*
         * Check survey status
         */

        if (survey.status !== "ACTIVE") {

            const container =
                document.getElementById("questionsContainer");

            if (container) {

                container.innerHTML = `
                    <div class="error-message">
                        <h3>Survey unavailable</h3>
                        <p>
                            This 360° feedback survey is no longer active.
                        </p>
                    </div>
                `;
            }

            return false;
        }

        return true;

    } catch (error) {

        console.error(
            "Failed to load survey details:",
            error
        );

        const container =
            document.getElementById("questionsContainer");

        if (container) {

            container.innerHTML = `
                <div class="error-message">
                    <h3>Unable to load survey</h3>
                    <p>
                        The survey link may be invalid or expired.
                    </p>
                </div>
            `;
        }

        return false;
    }
}


/* =========================================================
   LOAD QUESTIONS
   ========================================================= */

async function loadQuestions() {

    try {

        const response =
            await fetch(`${API_BASE_URL}/api/360/questions`);

        if (!response.ok) {
            throw new Error(`HTTP error: ${response.status}`);
        }

        const questions = await response.json();

        console.log("Questions loaded:", questions);

        const container =
            document.getElementById("questionsContainer");

        if (!container) {
            console.error(
                "Could not find questionsContainer"
            );
            return;
        }

        container.innerHTML = "";

        /*
         * Render each question
         */

        questions.forEach(question => {

            const questionElement =
                renderQuestion(question);

            container.appendChild(questionElement);

        });

    } catch (error) {

        console.error(
            "Failed to load questions:",
            error
        );

        const container =
            document.getElementById("questionsContainer");

        if (container) {

            container.innerHTML = `
                <div class="error-message">
                    <h3>Unable to load questions</h3>
                    <p>
                        Please refresh the page and try again.
                    </p>
                </div>
            `;
        }
    }
}


/* =========================================================
   RENDER INDIVIDUAL QUESTION
   ========================================================= */

function renderQuestion(question) {

    const questionDiv =
        document.createElement("div");

    questionDiv.className = "question";

    /*
     * Question number
     */

    const questionNumber =
        document.createElement("div");

    questionNumber.className =
        "question-number";

    questionNumber.textContent =
        `Q${question.questionNumber}`;

    questionDiv.appendChild(questionNumber);


    /*
     * Question text
     */

    const questionText =
        document.createElement("div");

    questionText.className =
        "question-text";

    /*
     * Replace [Name] with actual leader name
     */

    questionText.textContent =
        question.questionText.replace(
            /\[Name\]/g,
            currentLeaderName
        );

    questionDiv.appendChild(questionText);


    /* =====================================================
       RATING QUESTIONS
       Q1 - Q8
       ===================================================== */

    if (question.questionType === "RATING") {

        const ratingContainer =
            document.createElement("div");

        ratingContainer.className =
            "rating-options";


        /*
         * Create ratings 1-5
         */

        for (let rating = 1; rating <= 5; rating++) {

            const option =
                document.createElement("div");

            option.className =
                "rating-option";


            const input =
                document.createElement("input");

            input.type = "radio";

            input.name =
                `question-${question.id}`;

            input.value = rating;

            input.id =
                `question-${question.id}-${rating}`;


            const label =
                document.createElement("label");

            label.htmlFor =
                input.id;

            label.textContent =
                rating;


            option.appendChild(input);

            option.appendChild(label);

            ratingContainer.appendChild(option);
        }


        questionDiv.appendChild(
            ratingContainer
        );


        /*
         * Rating labels
         */

        const ratingLabels =
            document.createElement("div");

        ratingLabels.className =
            "rating-labels";

        ratingLabels.innerHTML = `
            <span>1 - Poor</span>
            <span>2</span>
            <span>3 - Average</span>
            <span>4</span>
            <span>5 - Excellent</span>
        `;

        questionDiv.appendChild(
            ratingLabels
        );
    }


    /* =====================================================
       TEXT QUESTIONS
       Q9, Q12, Q13, Q14
       ===================================================== */

    else if (question.questionType === "TEXT") {

        const textarea =
            document.createElement("textarea");

        textarea.name =
            `question-${question.id}`;

        textarea.id =
            `question-${question.id}`;

        textarea.placeholder =
            "Enter your response...";

        textarea.rows = 5;

        questionDiv.appendChild(
            textarea
        );
    }


    /* =====================================================
       MULTI SELECT
       Q10 - Q11
       ===================================================== */

    else if (
        question.questionType === "MULTI_SELECT"
    ) {

        const instruction =
            document.createElement("p");

        instruction.className =
            "selection-instruction";

        instruction.textContent =
            "Select up to 3 options.";

        questionDiv.appendChild(
            instruction
        );


        const optionsContainer =
            document.createElement("div");

        optionsContainer.className =
            "checkbox-options";


        /*
         * Make sure options exist
         */

        if (
            question.options &&
            question.options.length > 0
        ) {

            question.options.forEach(
                optionData => {

                    const optionWrapper =
                        document.createElement("div");

                    optionWrapper.className =
                        "checkbox-option";


                    const checkbox =
                        document.createElement("input");

                    checkbox.type = "checkbox";

                    checkbox.name =
                        `question-${question.id}`;

                    checkbox.value =
                        optionData.id;

                    checkbox.id =
                        `question-${question.id}-option-${optionData.id}`;


                    const label =
                        document.createElement("label");

                    label.htmlFor =
                        checkbox.id;

                    label.textContent =
                        optionData.optionText;


                    /*
                     * Limit selection to 3
                     */

                    checkbox.addEventListener(
                        "change",
                        () => {

                            const selected =
                                document.querySelectorAll(
                                    `input[name="question-${question.id}"]:checked`
                                );

                            const unchecked =
                                document.querySelectorAll(
                                    `input[name="question-${question.id}"]:not(:checked)`
                                );


                            if (
                                selected.length >= 3
                            ) {

                                unchecked.forEach(
                                    input => {
                                        input.disabled = true;
                                    }
                                );

                            } else {

                                document
                                    .querySelectorAll(
                                        `input[name="question-${question.id}"]`
                                    )
                                    .forEach(
                                        input => {
                                            input.disabled = false;
                                        }
                                    );
                            }

                        }
                    );


                    optionWrapper.appendChild(
                        checkbox
                    );

                    optionWrapper.appendChild(
                        label
                    );

                    optionsContainer.appendChild(
                        optionWrapper
                    );

                }
            );

        } else {

            optionsContainer.innerHTML =
                "<p>No options available.</p>";
        }


        questionDiv.appendChild(
            optionsContainer
        );
    }


    /* =====================================================
       UNKNOWN QUESTION TYPE
       ===================================================== */

    else {

        console.warn(
            "Unknown question type:",
            question.questionType
        );

        const warning =
            document.createElement("p");

        warning.textContent =
            "This question type is not currently supported.";

        questionDiv.appendChild(
            warning
        );
    }


    return questionDiv;
}


/* =========================================================
   COLLECT RESPONSES
   ========================================================= */

function collectResponses() {

    const responses = [];

    const questionElements =
        document.querySelectorAll(".question");


    questionElements.forEach(
        questionElement => {

            const numberElement =
                questionElement.querySelector(
                    ".question-number"
                );

            if (!numberElement) {
                return;
            }


            const questionNumber =
                Number(
                    numberElement.textContent
                        .replace("Q", "")
                );


            /*
             * Rating
             */

            const selectedRating =
                questionElement.querySelector(
                    'input[type="radio"]:checked'
                );

            if (selectedRating) {

                responses.push({

                    questionNumber:
                        questionNumber,

                    answer:
                        selectedRating.value

                });

                return;
            }


            /*
             * Text
             */

            const textarea =
                questionElement.querySelector(
                    "textarea"
                );

            if (textarea) {

                responses.push({

                    questionNumber:
                        questionNumber,

                    answer:
                        textarea.value.trim()

                });

                return;
            }


            /*
             * Multi-select
             */

            const selectedOptions =
                questionElement.querySelectorAll(
                    'input[type="checkbox"]:checked'
                );


            if (
                selectedOptions.length > 0
            ) {

                const options =
                    Array.from(
                        selectedOptions
                    ).map(
                        option => option.value
                    );


                responses.push({

                    questionNumber:
                        questionNumber,

                    answer:
                        options

                });
            }

        }
    );


    return responses;
}


/* =========================================================
   VALIDATE SURVEY
   ========================================================= */

function validateSurvey() {

    const questions =
        document.querySelectorAll(
            ".question"
        );

    let valid = true;


    questions.forEach(
        question => {

            /*
             * Rating question
             */

            const rating =
                question.querySelector(
                    'input[type="radio"]'
                );

            if (rating) {

                const selected =
                    question.querySelector(
                        'input[type="radio"]:checked'
                    );


                if (!selected) {

                    question.classList.add(
                        "question-error"
                    );

                    valid = false;

                } else {

                    question.classList.remove(
                        "question-error"
                    );
                }

                return;
            }


            /*
             * Text question
             */

            const textarea =
                question.querySelector(
                    "textarea"
                );

            if (textarea) {

                if (
                    textarea.value.trim() === ""
                ) {

                    question.classList.add(
                        "question-error"
                    );

                    valid = false;

                } else {

                    question.classList.remove(
                        "question-error"
                    );
                }

                return;
            }


            /*
             * Multi-select question
             */

            const checkboxes =
                question.querySelectorAll(
                    'input[type="checkbox"]'
                );


            if (checkboxes.length > 0) {

                const selected =
                    question.querySelectorAll(
                        'input[type="checkbox"]:checked'
                    );


                if (
                    selected.length === 0
                ) {

                    question.classList.add(
                        "question-error"
                    );

                    valid = false;

                } else {

                    question.classList.remove(
                        "question-error"
                    );
                }
            }

        }
    );


    if (!valid) {

        alert(
            "Please answer all questions before submitting."
        );

        /*
         * Scroll to first unanswered question
         */

        const firstError =
            document.querySelector(
                ".question-error"
            );

        if (firstError) {

            firstError.scrollIntoView({
                behavior: "smooth",
                block: "center"
            });
        }
    }


    return valid;
}


/* =========================================================
   SUBMIT SURVEY
   ========================================================= */

function submitSurvey() {

    /*
     * Validate
     */

    if (!validateSurvey()) {
        return;
    }


    /*
     * Collect answers
     */

    const responses =
        collectResponses();


    /*
     * Display in console for now
     */

    console.log(
        "Survey ID:",
        currentSurveyId
    );

    console.log(
        "Survey token:",
        currentToken
    );

    console.log(
        "Leader:",
        currentLeaderName
    );

    console.log(
        "Responses:",
        responses
    );


    /*
     * We will connect this to the
     * Spring Boot POST endpoint next.
     */

    alert(
        "Thank you! Your feedback has been recorded."
    );
}


/* =========================================================
   INITIALISE PAGE
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        /*
         * First get the survey/leader
         */

        const surveyLoaded =
            await loadSurveyDetails();


        /*
         * Only load questions if the survey
         * exists and is active.
         */

        if (surveyLoaded) {

            await loadQuestions();
        }


        /*
         * Submit button
         */

        const submitButton =
            document.getElementById(
                "submitSurvey"
            );


        if (submitButton) {

            submitButton.addEventListener(
                "click",
                submitSurvey
            );
        }

    }
);