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


/* =========================================================
   ADMIN SECTION
   ========================================================= */

if (
    role === "ADMIN"
    &&
    adminSection
) {

    adminSection.classList.remove(
        "d-none"
    );

}


/* =========================================================
   LEADERSHIP ASSESSMENT
   ========================================================= */

if (surveyBtn) {

    surveyBtn.addEventListener(
        "click",
        () => {

            window.location.href =
                "survey.html";

        }
    );

}


/* =========================================================
   CREATE 360 SURVEY
   ========================================================= */

if (generate360Btn) {

    generate360Btn.addEventListener(
        "click",
        generate360Survey
    );

}


async function generate360Survey() {

    try {

        generate360Btn.disabled =
            true;

        generate360Btn.textContent =
            "Creating Survey...";


        const response =
            await fetch(
                `${API_BASE_URL}/api/360/surveys`,
                {
                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/json",

                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        if (!response.ok) {

            throw new Error(
                `Failed to create survey: ${response.status}`
            );

        }


        const data =
            await response.json();


        console.log(
            "360 survey returned:",
            data
        );


        /*
         * The backend may either:
         *
         * 1. create a new survey, or
         * 2. return an existing active survey.
         */
        const link =
            build360SurveyLink(
                data.token
            );


        showActive360Survey(
            {
                id: data.id,

                token:
                    data.token,

                status:
                    data.status ||
                    "ACTIVE",

                responseCount:
                    0,

                link:
                    link
            }
        );


        /*
         * Reload active survey from backend
         * so expiry and response count are current.
         */
        await loadActive360Survey();


        /*
         * Reload history as well.
         */
        await load360SurveyHistory();


    } catch (error) {

        console.error(
            "Unable to create 360 survey:",
            error
        );


        alert(
            "Unable to create 360 feedback survey."
        );


    } finally {

        generate360Btn.disabled =
            false;

        generate360Btn.textContent =
            "Create 360° Feedback Link";

    }

}


/* =========================================================
   GET CURRENT ACTIVE SURVEY
   ========================================================= */

async function loadActive360Survey() {

    try {

        const response =
            await fetch(
                `${API_BASE_URL}/api/360/surveys/me`,
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
                `Failed to load active survey: ${response.status}`
            );

        }


        const data =
            await response.json();


        console.log(
            "Active 360 survey:",
            data
        );


        /*
         * No current active survey.
         */
        if (!data.hasActiveSurvey) {

            showNoActive360Survey();

            return;

        }


        /*
         * Active survey exists.
         */
        const link =
            build360SurveyLink(
                data.token
            );


        showActive360Survey(
            {
                id:
                    data.id,

                token:
                    data.token,

                status:
                    data.status ||
                    "ACTIVE",

                responseCount:
                    data.responseCount ?? 0,

                createdAt:
                    data.createdAt,

                expiresAt:
                    data.expiresAt,

                link:
                    link
            }
        );


    } catch (error) {

        console.error(
            "Unable to load active 360 survey:",
            error
        );

    }

}


/* =========================================================
   DISPLAY NO ACTIVE SURVEY
   ========================================================= */

function showNoActive360Survey() {

    current360SurveyId =
        null;

    current360SurveyToken =
        null;


    if (no360SurveyState) {

        no360SurveyState.style.display =
            "block";

    }


    if (active360SurveyState) {

        active360SurveyState.style.display =
            "none";

    }


    if (surveyStatusBadge) {

        surveyStatusBadge.style.display =
            "none";

    }


    if (responseCount) {

        responseCount.textContent =
            "0";

    }


    if (surveyIdDisplay) {

        surveyIdDisplay.textContent =
            "-";

    }


    if (surveyExpiry) {

        surveyExpiry.textContent =
            "-";

    }


    if (surveyLink) {

        surveyLink.value =
            "";

    }

}


/* =========================================================
   DISPLAY ACTIVE SURVEY
   ========================================================= */

function showActive360Survey(data) {

    current360SurveyId =
        Number(data.id);

    current360SurveyToken =
        data.token;


    /*
     * Hide create survey state.
     */
    if (no360SurveyState) {

        no360SurveyState.style.display =
            "none";

    }


    /*
     * Show active survey state.
     */
    if (active360SurveyState) {

        active360SurveyState.style.display =
            "block";

    }


    /*
     * Survey ID.
     */
    if (surveyIdDisplay) {

        surveyIdDisplay.textContent =
            `#${data.id}`;

    }


    /*
     * Response count.
     */
    if (responseCount) {

        responseCount.textContent =
            data.responseCount ?? 0;

    }


    /*
     * Survey status.
     */
    if (surveyStatus) {

        surveyStatus.textContent =
            data.status ||
            "ACTIVE";

    }


    /*
     * Status badge.
     */
    if (surveyStatusBadge) {

        surveyStatusBadge.style.display =
            "inline-block";

        surveyStatusBadge.textContent =
            data.status ||
            "ACTIVE";


        if (
            data.status === "ACTIVE"
        ) {

            surveyStatusBadge.className =
                "badge bg-success";

        } else {

            surveyStatusBadge.className =
                "badge bg-secondary";

        }

    }


    /*
     * Reviewer link.
     */
    if (surveyLink) {

        surveyLink.value =
            data.link || "";

    }


    /*
     * Expiry date.
     */
    if (surveyExpiry) {

        surveyExpiry.textContent =
            formatDate(
                data.expiresAt
            );

    }

}


/* =========================================================
   LOAD SURVEY HISTORY
   ========================================================= */

async function load360SurveyHistory() {

    if (!surveyHistoryContainer) {

        return;

    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/api/360/surveys/me/history`,
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
                `Failed to load survey history: ${response.status}`
            );

        }


        const surveys =
            await response.json();


        console.log(
            "360 survey history:",
            surveys
        );


        render360SurveyHistory(
            surveys
        );


    } catch (error) {

        console.error(
            "Unable to load 360 survey history:",
            error
        );


        surveyHistoryContainer.innerHTML = `
            <p class="text-muted">
                Unable to load previous surveys.
            </p>
        `;

    }

}


/* =========================================================
   RENDER SURVEY HISTORY
   ========================================================= */

function render360SurveyHistory(
    surveys
) {

    if (!surveyHistoryContainer) {

        return;

    }


    surveyHistoryContainer.innerHTML =
        "";


    /*
     * No previous surveys.
     */
    if (
        !Array.isArray(surveys)
        ||
        surveys.length === 0
    ) {

        surveyHistoryContainer.innerHTML = `
            <p class="text-muted">
                You do not have any previous
                360° feedback surveys yet.
            </p>
        `;

        return;

    }


    /*
     * Render each previous survey.
     */
    surveys.forEach(
        survey => {

            const card =
                document.createElement(
                    "div"
                );


            card.className =
                "card mb-3";


            const createdDate =
                formatDate(
                    survey.createdAt
                );


            const expiryDate =
                formatDate(
                    survey.expiresAt
                );


            const responseLabel =
                survey.responseCount === 1
                    ? "response"
                    : "responses";


            let statusClass =
                "bg-secondary";


            if (
                survey.status === "ACTIVE"
            ) {

                statusClass =
                    "bg-success";

            }


            card.innerHTML = `

                <div class="card-body">

                    <div
                        class="
                            d-flex
                            justify-content-between
                            align-items-start
                            flex-wrap
                            gap-3
                        "
                    >

                        <div>

                            <h5 class="mb-1">
                                360° Feedback Survey
                                #${survey.id}
                            </h5>

                            <p
                                class="
                                    text-muted
                                    mb-1
                                "
                            >
                                ${createdDate}
                                –
                                ${expiryDate}
                            </p>

                            <p class="mb-0">
                                ${survey.responseCount}
                                ${responseLabel}
                            </p>

                        </div>


                        <div
                            class="
                                d-flex
                                align-items-center
                                gap-2
                                flex-wrap
                            "
                        >

                            <span
                                class="
                                    badge
                                    ${statusClass}
                                "
                            >
                                ${survey.status}
                            </span>


                            <button
                                type="button"
                                class="
                                    btn
                                    btn-outline-primary
                                    btn-sm
                                    view-history-results-btn
                                "
                                data-survey-id="${survey.id}"
                            >
                                View Results
                            </button>

                        </div>

                    </div>

                </div>
            `;


            surveyHistoryContainer
                .appendChild(
                    card
                );

        }
    );


    /*
     * Attach View Results event listeners
     * after rendering all cards.
     */
    const historyButtons =
        document.querySelectorAll(
            ".view-history-results-btn"
        );


    historyButtons.forEach(
        button => {

            button.addEventListener(
                "click",
                () => {

                    const surveyId =
                        button.dataset
                                .surveyId;


                    if (!surveyId) {

                        return;

                    }


                    window.location.href =
                        `360-results.html?surveyId=${surveyId}`;

                }
            );

        }
    );

}


/* =========================================================
   BUILD REVIEWER LINK
   ========================================================= */

function build360SurveyLink(
    surveyToken
) {

    /*
     * Local development URL.
     *
     * Example:
     *
     * http://127.0.0.1:5500/frontend/
     * 360-feedback.html?token=...
     */
    return (
        `${window.location.origin}` +
        `/frontend/360-feedback.html` +
        `?token=${surveyToken}`
    );

}


/* =========================================================
   FORMAT DATE
   ========================================================= */

function formatDate(
    dateString
) {

    if (!dateString) {

        return "-";

    }


    const date =
        new Date(
            dateString
        );


    /*
     * Protect against an invalid date.
     */
    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return "-";

    }


    return date.toLocaleDateString(
        "en-AU",
        {
            day: "numeric",
            month: "short",
            year: "numeric"
        }
    );

}


/* =========================================================
   COPY SURVEY LINK
   ========================================================= */

async function copySurveyLink() {

    if (!surveyLink) {

        return;

    }


    const link =
        surveyLink.value;


    if (!link) {

        alert(
            "No survey link is available."
        );

        return;

    }


    try {

        await navigator.clipboard
            .writeText(
                link
            );


        alert(
            "360° feedback link copied!"
        );


    } catch (error) {

        console.error(
            "Unable to copy survey link:",
            error
        );


        /*
         * Fallback for older browsers.
         */
        surveyLink.select();


        document.execCommand(
            "copy"
        );


        alert(
            "360° feedback link copied!"
        );

    }

}


/* =========================================================
   COPY BUTTON LISTENERS
   ========================================================= */

if (copySurveyLinkBtn) {

    copySurveyLinkBtn.addEventListener(
        "click",
        copySurveyLink
    );

}


if (copySurveyLinkSecondaryBtn) {

    copySurveyLinkSecondaryBtn
        .addEventListener(
            "click",
            copySurveyLink
        );

}


/* =========================================================
   VIEW CURRENT SURVEY RESULTS
   ========================================================= */

if (view360ResultsBtn) {

    view360ResultsBtn.addEventListener(
        "click",
        () => {

            if (!current360SurveyId) {

                alert(
                    "No 360° feedback survey is available."
                );

                return;

            }


            window.location.href =
                `360-results.html?surveyId=${current360SurveyId}`;

        }
    );

}


/* =========================================================
   LOGOUT
   ========================================================= */

if (logoutBtn) {

    logoutBtn.addEventListener(
        "click",
        () => {

            /*
             * Only authentication data
             * is stored in localStorage now.
             *
             * Survey data lives in the database.
             */
            localStorage.removeItem(
                "token"
            );


            localStorage.removeItem(
                "role"
            );


            window.location.href =
                "index.html";

        }
    );

}


/* =========================================================
   PAGE INITIALISATION
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        /*
         * Load the current 30-day survey.
         */
        await loadActive360Survey();


        /*
         * Load previous survey periods.
         */
        await load360SurveyHistory();

    }
);