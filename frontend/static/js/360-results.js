const API_BASE_URL = "http://localhost:8080";


/* =========================================================
   GET SURVEY ID
   Example:
   360-results.html?surveyId=16
   ========================================================= */

function getSurveyId() {

    const params =
        new URLSearchParams(
            window.location.search
        );

    return params.get("surveyId");
}


/* =========================================================
   GET AUTH TOKEN
   ========================================================= */

function getAuthToken() {

    return localStorage.getItem("token");
}


/* =========================================================
   LOAD RESULTS
   ========================================================= */

async function loadResults() {

    const surveyId =
        getSurveyId();

    const token =
        getAuthToken();


    if (!surveyId) {

        showError(
            "No survey ID was provided."
        );

        return;
    }


    if (!token) {

        showError(
            "You must be logged in to view your 360 feedback results."
        );

        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/api/360/surveys/${surveyId}/results`,
                {
                    method: "GET",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        if (!response.ok) {

            throw new Error(
                `HTTP error: ${response.status}`
            );
        }


        const results =
            await response.json();


        console.log(
            "360 results:",
            results
        );


        renderResponseCount(
            results.responseCount
        );

        renderRatings(
            results.ratings
        );

        renderOptionResults(
            results.optionResults
        );

        renderWrittenFeedback(
            results.writtenFeedback
        );


    } catch (error) {

        console.error(
            "Failed to load 360 results:",
            error
        );

        showError(
            "Unable to load your 360 feedback results."
        );
    }
}


/* =========================================================
   RESPONSE COUNT
   ========================================================= */

function renderResponseCount(count) {

    const element =
        document.getElementById(
            "responseCount"
        );

    if (element) {

        element.textContent =
            count ?? 0;
    }
}


/* =========================================================
   LEADERSHIP RATINGS
   Q1-Q8

   Displays:
   - Category
   - Question number
   - Average score
   - Visual progress bar
   - Overall average
   ========================================================= */

function renderRatings(ratings) {

    const container =
        document.getElementById(
            "ratingsContainer"
        );

    const overallAverageElement =
        document.getElementById(
            "overallAverage"
        );


    if (!container) {
        return;
    }


    container.innerHTML = "";


    if (
        !ratings ||
        ratings.length === 0
    ) {

        container.innerHTML = `
            <p class="empty-message">
                No rating results available yet.
            </p>
        `;


        if (overallAverageElement) {

            overallAverageElement.innerHTML =
                '- <span>/ 5</span>';
        }


        return;
    }


    let total = 0;
    let validScoreCount = 0;


    ratings.forEach(rating => {

        const score =
            Number(
                rating.averageScore
            );


        /*
         * Protect against invalid or missing
         * average scores.
         */

        const validScore =
            Number.isFinite(score)
                ? score
                : 0;


        if (Number.isFinite(score)) {

            total += score;

            validScoreCount++;
        }


        /*
         * Convert the score out of 5
         * into a percentage for the bar.
         */

        const percentage =
            Math.max(
                0,
                Math.min(
                    100,
                    (validScore / 5) * 100
                )
            );


        const card =
            document.createElement(
                "div"
            );

        card.className =
            "rating-card";


        card.innerHTML = `
            <div class="rating-card-top">

                <div>

                    <div class="rating-category">
                        ${escapeHtml(
                            rating.category
                        )}
                    </div>

                    <div class="rating-question">
                        Question
                        ${escapeHtml(
                            rating.questionNumber
                        )}
                    </div>

                </div>


                <div class="rating-score">

                    ${validScore.toFixed(1)}

                    <span>
                        / 5
                    </span>

                </div>

            </div>


            <div class="score-track">

                <div
                    class="score-fill"
                    style="width: ${percentage}%"
                ></div>

            </div>
        `;


        container.appendChild(
            card
        );
    });


    /*
     * Calculate overall average using
     * the Q1-Q8 competency averages.
     */

    if (overallAverageElement) {

        if (validScoreCount > 0) {

            const overallAverage =
                total /
                validScoreCount;


            overallAverageElement.innerHTML = `
                ${overallAverage.toFixed(1)}
                <span>/ 5</span>
            `;

        } else {

            overallAverageElement.innerHTML =
                '- <span>/ 5</span>';
        }
    }
}


/* =========================================================
   OPTION RESULTS
   Q10 = Strengths
   Q11 = Working Style
   ========================================================= */

function renderOptionResults(
    optionResults
) {

    const strengthsContainer =
        document.getElementById(
            "strengthsContainer"
        );


    const workingStyleContainer =
        document.getElementById(
            "workingStyleContainer"
        );


    /*
     * JSON object keys become strings,
     * therefore question 10 is "10"
     * and question 11 is "11".
     */

    const strengths =
        optionResults?.["10"] || [];


    const workingStyle =
        optionResults?.["11"] || [];


    renderOptionList(
        strengthsContainer,
        strengths
    );


    renderOptionList(
        workingStyleContainer,
        workingStyle
    );
}


/* =========================================================
   RENDER OPTION CHIPS
   ========================================================= */

function renderOptionList(
    container,
    options
) {

    if (!container) {
        return;
    }


    container.innerHTML = "";


    if (
        !options ||
        options.length === 0
    ) {

        container.innerHTML = `
            <p class="empty-message">
                No responses available yet.
            </p>
        `;

        return;
    }


    const list =
        document.createElement(
            "div"
        );


    list.className =
        "option-list";


    /*
     * Sort most commonly selected
     * options first.
     */

    const sortedOptions =
        [...options].sort(
            (a, b) =>
                Number(b.count) -
                Number(a.count)
        );


    sortedOptions.forEach(option => {

        const chip =
            document.createElement(
                "div"
            );


        chip.className =
            "feedback-chip";


        chip.innerHTML = `
            <span>
                ${escapeHtml(
                    option.optionText
                )}
            </span>

            <span
                class="feedback-chip-count"
                title="Number of reviewers who selected this option"
            >
                ${Number(option.count) || 0}
            </span>
        `;


        list.appendChild(
            chip
        );
    });


    container.appendChild(
        list
    );
}


/* =========================================================
   WRITTEN FEEDBACK
   Q9 + Q12-Q14
   ========================================================= */

function renderWrittenFeedback(
    writtenFeedback
) {

    const container =
        document.getElementById(
            "writtenFeedbackContainer"
        );


    if (!container) {
        return;
    }


    container.innerHTML = "";


    const sections = [

        {
            question: "9",

            title:
                "Examples of Strong Results or Positive Impact"
        },

        {
            question: "12",

            title:
                "What You Do Really Well"
        },

        {
            question: "13",

            title:
                "What You Could Do More Of"
        },

        {
            question: "14",

            title:
                "How Others Are Willing to Support You"
        }

    ];


    sections.forEach(section => {

        const responses =
            writtenFeedback?.[
                section.question
            ] || [];


        const sectionElement =
            document.createElement(
                "div"
            );


        sectionElement.className =
            "written-section";


        /*
         * Section heading
         */

        const heading =
            document.createElement(
                "h3"
            );


        heading.textContent =
            section.title;


        sectionElement.appendChild(
            heading
        );


        /*
         * No written responses
         */

        if (responses.length === 0) {

            const empty =
                document.createElement(
                    "p"
                );


            empty.className =
                "empty-message";


            empty.textContent =
                "No responses available yet.";


            sectionElement.appendChild(
                empty
            );

        } else {

            /*
             * Render each anonymous
             * response separately.
             */

            responses.forEach(
                responseText => {

                    const feedback =
                        document.createElement(
                            "div"
                        );


                    feedback.className =
                        "anonymous-feedback-item";


                    /*
                     * textContent is used deliberately
                     * so reviewer text cannot be
                     * interpreted as HTML.
                     */

                    feedback.textContent =
                        responseText;


                    sectionElement.appendChild(
                        feedback
                    );
                }
            );
        }


        container.appendChild(
            sectionElement
        );
    });
}


/* =========================================================
   ERROR MESSAGE
   ========================================================= */

function showError(message) {

    const element =
        document.getElementById(
            "errorMessage"
        );


    if (!element) {
        return;
    }


    element.textContent =
        message;


    element.style.display =
        "block";
}


/* =========================================================
   HTML ESCAPE

   Used for values inserted into
   dynamically generated HTML.
   ========================================================= */

function escapeHtml(value) {

    const div =
        document.createElement(
            "div"
        );


    div.textContent =
        value ?? "";


    return div.innerHTML;
}


/* =========================================================
   PAGE STARTUP
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    loadResults
);