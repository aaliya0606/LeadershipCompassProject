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

        const container =
            document.getElementById("questionsContainer");

        if (container) {
            container.innerHTML = `
                <div class="error-message">
                    <h3>Survey link required</h3>
                    <p>
                        Please open this survey using the unique
                        feedback link provided to you.
                    </p>
                </div>
            `;
        }

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
         * Update leader name in page heading
         */

        const leaderNameElement =
            document.getElementById("leaderName");

        if (leaderNameElement) {
            leaderNameElement.textContent =
                currentLeaderName;
        }


        /*
         * Check survey status
         */

        if (survey.status !== "ACTIVE") {

            const container =
                document.getElementById(
                    "questionsContainer"
                );

            if (container) {

                container.innerHTML = `
                    <div class="error-message">
                        <h3>Survey unavailable</h3>
                        <p>
                            This 360° feedback survey is
                            no longer active.
                        </p>
                    </div>
                `;
            }

            hideSubmitButton();

            return false;
        }

        return true;

    } catch (error) {

        console.error(
            "Failed to load survey details:",
            error
        );

        const container =
            document.getElementById(
                "questionsContainer"
            );

        if (container) {

            container.innerHTML = `
                <div class="error-message">
                    <h3>Unable to load survey</h3>
                    <p>
                        The survey link may be invalid
                        or expired.
                    </p>
                </div>
            `;
        }

        hideSubmitButton();

        return false;
    }
}


/* =========================================================
   LOAD QUESTIONS
   ========================================================= */

async function loadQuestions() {

    try {

        const response =
            await fetch(
                `${API_BASE_URL}/api/360/questions`
            );

        if (!response.ok) {
            throw new Error(
                `HTTP error: ${response.status}`
            );
        }

        const questions =
            await response.json();

        console.log(
            "Questions loaded:",
            questions
        );

        const container =
            document.getElementById(
                "questionsContainer"
            );

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

            container.appendChild(
                questionElement
            );
        });

    } catch (error) {

        console.error(
            "Failed to load questions:",
            error
        );

        const container =
            document.getElementById(
                "questionsContainer"
            );

        if (container) {

            container.innerHTML = `
                <div class="error-message">
                    <h3>Unable to load questions</h3>
                    <p>
                        Please refresh the page
                        and try again.
                    </p>
                </div>
            `;
        }

        hideSubmitButton();
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
     * Store database information on the element.
     * This is used when building the submission DTO.
     */

    questionDiv.dataset.questionId =
        question.id;

    questionDiv.dataset.questionType =
        question.questionType;


    /*
     * Question number
     */

    const questionNumber =
        document.createElement("div");

    questionNumber.className =
        "question-number";

    questionNumber.textContent =
        `Q${question.questionNumber}`;

    questionDiv.appendChild(
        questionNumber
    );


    /*
     * Question text
     */

    const questionText =
        document.createElement("div");

    questionText.className =
        "question-text";

    /*
     * Replace [Name] with leader name
     */

    questionText.textContent =
        question.questionText.replace(
            /\[Name\]/g,
            currentLeaderName
        );

    questionDiv.appendChild(
        questionText
    );


    /* =====================================================
       RATING QUESTIONS
       Q1-Q8
       ===================================================== */

    if (question.questionType === "RATING") {

        const ratingContainer =
            document.createElement("div");

        ratingContainer.className =
            "rating-options";


        for (
            let rating = 1;
            rating <= 5;
            rating++
        ) {

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

            ratingContainer.appendChild(
                option
            );
        }


        questionDiv.appendChild(
            ratingContainer
        );


        /*
         * Rating explanation
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
       Q9, Q12-Q14
       ===================================================== */

    else if (
        question.questionType === "TEXT"
    ) {

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
       Q10-Q11
       ===================================================== */

    else if (
        question.questionType ===
        "MULTI_SELECT"
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


        if (
            question.options &&
            question.options.length > 0
        ) {

            question.options.forEach(
                optionData => {

                    const optionWrapper =
                        document.createElement(
                            "div"
                        );

                    optionWrapper.className =
                        "checkbox-option";


                    const checkbox =
                        document.createElement(
                            "input"
                        );

                    checkbox.type =
                        "checkbox";

                    checkbox.name =
                        `question-${question.id}`;

                    checkbox.value =
                        optionData.id;

                    checkbox.id =
                        `question-${question.id}-option-${optionData.id}`;


                    const label =
                        document.createElement(
                            "label"
                        );

                    label.htmlFor =
                        checkbox.id;

                    label.textContent =
                        optionData.optionText;


                    /*
                     * Limit to 3 selections.
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
                                        input.disabled =
                                            true;
                                    }
                                );

                            } else {

                                document
                                    .querySelectorAll(
                                        `input[name="question-${question.id}"]`
                                    )
                                    .forEach(
                                        input => {
                                            input.disabled =
                                                false;
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
       UNKNOWN TYPE
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
   Builds the same structure expected by:
   Feedback360SubmissionDTO
   ========================================================= */

function collectResponses() {

    const answers = [];

    const questionElements =
        document.querySelectorAll(
            ".question"
        );


    questionElements.forEach(
        questionElement => {

            const questionId =
                Number(
                    questionElement
                        .dataset
                        .questionId
                );

            const questionType =
                questionElement
                    .dataset
                    .questionType;


            /*
             * Q1-Q8: Rating
             */

            if (
                questionType === "RATING"
            ) {

                const selectedRating =
                    questionElement.querySelector(
                        'input[type="radio"]:checked'
                    );

                answers.push({

                    questionId:
                        questionId,

                    score:
                        selectedRating
                            ? Number(
                                selectedRating.value
                            )
                            : null,

                    textResponse:
                        null,

                    selectedOptionIds:
                        []
                });
            }


            /*
             * Q9, Q12-Q14: Text
             */

            else if (
                questionType === "TEXT"
            ) {

                const textarea =
                    questionElement.querySelector(
                        "textarea"
                    );

                answers.push({

                    questionId:
                        questionId,

                    score:
                        null,

                    textResponse:
                        textarea
                            ? textarea.value.trim()
                            : "",

                    selectedOptionIds:
                        []
                });
            }


            /*
             * Q10-Q11: Multi-select
             */

            else if (
                questionType ===
                "MULTI_SELECT"
            ) {

                const selectedOptions =
                    questionElement
                        .querySelectorAll(
                            'input[type="checkbox"]:checked'
                        );

                const selectedOptionIds =
                    Array.from(
                        selectedOptions
                    ).map(
                        option =>
                            Number(
                                option.value
                            )
                    );


                answers.push({

                    questionId:
                        questionId,

                    score:
                        null,

                    textResponse:
                        null,

                    selectedOptionIds:
                        selectedOptionIds
                });
            }
        }
    );


    return answers;
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
             * Remove any old error first.
             */

            question.classList.remove(
                "question-error"
            );


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
                    textarea
                        .value
                        .trim() === ""
                ) {

                    question.classList.add(
                        "question-error"
                    );

                    valid = false;
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


            if (
                checkboxes.length > 0
            ) {

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
                }


                if (
                    selected.length > 3
                ) {

                    question.classList.add(
                        "question-error"
                    );

                    valid = false;
                }
            }
        }
    );


    if (!valid) {

        alert(
            "Please answer all questions before submitting."
        );

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
   HTML ESCAPE
   Used when displaying leader name in success message
   ========================================================= */

function escapeHtml(value) {

    const div =
        document.createElement("div");

    div.textContent =
        value ?? "";

    return div.innerHTML;
}


/* =========================================================
   SUBMIT SURVEY
   Sends anonymous feedback to Spring Boot
   ========================================================= */

async function submitSurvey(event) {

    /*
     * Prevent form/button default behaviour.
     */

    if (event) {
        event.preventDefault();
    }


    /*
     * Validate first.
     */

    if (!validateSurvey()) {
        return;
    }


    if (!currentToken) {

        alert(
            "Survey token is missing."
        );

        return;
    }


    /*
     * Build backend DTO payload.
     */

    const answers =
        collectResponses();
        
    const submission = {
        answers: answers
    };

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
        "Submitting:",
        submission
    );


    const submitButton =
        document.getElementById(
            "submitSurvey"
        );


    try {

        /*
         * Prevent duplicate clicks while
         * request is being processed.
         */

        if (submitButton) {

            submitButton.disabled =
                true;

            submitButton.textContent =
                "Submitting...";
        }


        const response =
            await fetch(
                `${API_BASE_URL}/api/360/surveys/${currentToken}/responses`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            submission
                        )
                }
            );


        /*
         * Read backend response.
         */

        const responseText =
            await response.text();

        let responseData = null;


        if (responseText) {

            try {

                responseData =
                    JSON.parse(
                        responseText
                    );

            } catch {

                responseData = {
                    message:
                        responseText
                };
            }
        }


        /*
         * Backend returned an error.
         */

        if (!response.ok) {

            console.error(
                "Submission failed:",
                response.status,
                responseData
            );

            throw new Error(
                responseData?.message ||
                `HTTP error: ${response.status}`
            );
        }


        console.log(
            "360 feedback submitted successfully:",
            responseData
        );


        /*
         * Replace questions with success message.
         */

        const container =
            document.getElementById(
                "questionsContainer"
            );


        if (container) {

            container.innerHTML = `
                <div class="success-message">
                    <h2>Thank you!</h2>

                    <p>
                        Your anonymous feedback for
                        <strong>
                            ${escapeHtml(
                                currentLeaderName
                            )}
                        </strong>
                        has been submitted successfully.
                    </p>
                </div>
            `;
        }


        /*
         * Hide submit button after success.
         */

        if (submitButton) {

            submitButton.style.display =
                "none";
        }


        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });


    } catch (error) {

        console.error(
            "Failed to submit 360 feedback:",
            error
        );


        alert(
            "Your feedback could not be submitted. " +
            "Please try again."
        );


        /*
         * Allow retry.
         */

        if (submitButton) {

            submitButton.disabled =
                false;

            submitButton.textContent =
                "Submit Feedback";
        }
    }
}


/* =========================================================
   HIDE SUBMIT BUTTON
   ========================================================= */

function hideSubmitButton() {

    const submitButton =
        document.getElementById(
            "submitSurvey"
        );

    if (submitButton) {
        submitButton.style.display =
            "none";
    }
}


/* =========================================================
   INITIALISE PAGE
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        /*
         * Load survey/leader first.
         */

        const surveyLoaded =
            await loadSurveyDetails();


        /*
         * Only display questions for a
         * valid active survey.
         */

        if (surveyLoaded) {

            await loadQuestions();
        }


        /*
         * Connect submit button.
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