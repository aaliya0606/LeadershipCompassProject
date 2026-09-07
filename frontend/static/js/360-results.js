const API_BASE_URL = "http://localhost:8080";


/*
 * Get survey ID from URL.
 *
 * Example:
 * 360-results.html?surveyId=16
 */
function getSurveyId() {

    const params =
        new URLSearchParams(
            window.location.search
        );

    return params.get("surveyId");
}


/*
 * Get JWT stored by your login flow.
 *
 * Change "token" if your application
 * stores it under another key.
 */
function getAuthToken() {

    return localStorage.getItem("token");
}


/*
 * Load aggregated 360 results.
 */
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


/*
 * Response count
 */
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


/*
 * Q1-Q8 averages
 */
function renderRatings(ratings) {

    const container =
        document.getElementById(
            "ratingsContainer"
        );


    if (!container) {
        return;
    }


    container.innerHTML = "";


    if (!ratings ||
        ratings.length === 0) {

        container.innerHTML =
            "<p>No rating results available yet.</p>";

        return;
    }


    ratings.forEach(rating => {

        const column =
            document.createElement("div");

        column.className =
            "col-md-6 col-lg-4";


        column.innerHTML = `
            <div class="card h-100">

                <div class="card-body">

                    <h5 class="card-title">
                        ${escapeHtml(
                            rating.category
                        )}
                    </h5>

                    <p class="mb-1">
                        Question
                        ${rating.questionNumber}
                    </p>

                    <p class="display-6 mb-0">
                        ${Number(
                            rating.averageScore
                        ).toFixed(1)}
                        <span class="fs-6">
                            / 5
                        </span>
                    </p>

                </div>

            </div>
        `;


        container.appendChild(
            column
        );
    });
}


/*
 * Q10 and Q11
 */
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
     * JSON object keys become strings.
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


/*
 * Render option counts.
 */
function renderOptionList(
    container,
    options
) {

    if (!container) {
        return;
    }


    if (!options ||
        options.length === 0) {

        container.innerHTML =
            "<p>No responses available yet.</p>";

        return;
    }


    const list =
        document.createElement("div");

    list.className =
        "list-group";


    options.forEach(option => {

        const item =
            document.createElement("div");

        item.className =
            "list-group-item d-flex justify-content-between align-items-center";


        item.innerHTML = `
            <span>
                ${escapeHtml(
                    option.optionText
                )}
            </span>

            <span
                class="badge bg-secondary rounded-pill"
            >
                ${option.count}
            </span>
        `;


        list.appendChild(
            item
        );
    });


    container.innerHTML = "";

    container.appendChild(
        list
    );
}


/*
 * Q9 + Q12-Q14
 */
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
            document.createElement("div");

        sectionElement.className =
            "mb-4";


        const heading =
            document.createElement("h4");

        heading.textContent =
            section.title;


        sectionElement.appendChild(
            heading
        );


        if (responses.length === 0) {

            const empty =
                document.createElement("p");

            empty.className =
                "text-muted";

            empty.textContent =
                "No responses available yet.";

            sectionElement.appendChild(
                empty
            );

        } else {

            responses.forEach(
                responseText => {

                    const card =
                        document.createElement(
                            "div"
                        );

                    card.className =
                        "card mb-2";


                    card.innerHTML = `
                        <div class="card-body">
                            ${escapeHtml(
                                responseText
                            )}
                        </div>
                    `;


                    sectionElement.appendChild(
                        card
                    );
                }
            );
        }


        container.appendChild(
            sectionElement
        );
    });
}


/*
 * Error message
 */
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


/*
 * Prevent feedback text from being
 * interpreted as HTML.
 */
function escapeHtml(value) {

    const div =
        document.createElement("div");

    div.textContent =
        value ?? "";

    return div.innerHTML;
}


/*
 * Page startup
 */
document.addEventListener(
    "DOMContentLoaded",
    loadResults
);