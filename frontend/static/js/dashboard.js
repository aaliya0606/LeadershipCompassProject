const API_BASE_URL = "http://localhost:8080";

const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

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
    document.getElementById("copySurveyLinkSecondaryBtn");

const view360ResultsBtn =
    document.getElementById("view360ResultsBtn");

const surveyStatus =
    document.getElementById("surveyStatus");

const surveyStatusBadge =
    document.getElementById("surveyStatusBadge");

const responseCount =
    document.getElementById("responseCount");

const surveyIdDisplay =
    document.getElementById("surveyIdDisplay");


/*
 * Current survey information.
 *
 * This is kept in memory while the dashboard is open.
 * It is no longer restored from localStorage.
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
         * createSurvey() now returns either:
         *
         * - a newly created survey
         * OR
         * - the user's existing active survey.
         */
        const link =
            build360SurveyLink(
                data.token
            );


        showActive360Survey(
            {
                id: data.id,
                token: data.token,
                status:
                    data.status || "ACTIVE",
                responseCount: 0,
                link: link
            }
        );


        /*
         * Reload from the backend so the dashboard
         * gets the current response count and expiry.
         */
        await loadActive360Survey();


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

/*
 * Called whenever the dashboard loads.
 *
 * The backend decides whether the logged-in user
 * currently has an active 360 survey.
 */
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
                id: data.id,
                token: data.token,
                status:
                    data.status || "ACTIVE",
                responseCount:
                    data.responseCount ?? 0,
                createdAt:
                    data.createdAt,
                expiresAt:
                    data.expiresAt,
                link: link
            }
        );


    } catch (error) {

        console.error(
            "Unable to load active 360 survey:",
            error
        );


        /*
         * Don't automatically show the create button
         * if the backend failed.
         *
         * Otherwise a temporary network error could
         * make it look like the user has no survey.
         */
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
            data.status || "ACTIVE";

    }


    /*
     * Status badge.
     */
    if (surveyStatusBadge) {

        surveyStatusBadge.style.display =
            "inline-block";

        surveyStatusBadge.textContent =
            data.status || "ACTIVE";


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
     * Optional expiry display.
     *
     * This only does anything if your HTML contains:
     *
     * <span id="surveyExpiry"></span>
     */
    const surveyExpiry =
        document.getElementById(
            "surveyExpiry"
        );


    if (
        surveyExpiry
        &&
        data.expiresAt
    ) {

        surveyExpiry.textContent =
            formatDate(
                data.expiresAt
            );

    }

}


/* =========================================================
   BUILD REVIEWER LINK
   ========================================================= */

function build360SurveyLink(
    surveyToken
) {

    /*
     * If your Live Server URL contains /frontend/,
     * this generates:
     *
     * http://127.0.0.1:5500/frontend/360-feedback.html?token=...
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

function formatDate(dateString) {

    if (!dateString) {

        return "-";

    }


    const date =
        new Date(dateString);


    return date.toLocaleDateString(
        "en-AU",
        {
            day: "numeric",
            month: "long",
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

        await navigator.clipboard.writeText(
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
         * Fallback for browsers where
         * Clipboard API is unavailable.
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


if (copySurveyLinkBtn) {

    copySurveyLinkBtn.addEventListener(
        "click",
        copySurveyLink
    );

}


if (copySurveyLinkSecondaryBtn) {

    copySurveyLinkSecondaryBtn.addEventListener(
        "click",
        copySurveyLink
    );

}


/* =========================================================
   VIEW RESULTS
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
             * Only authentication information
             * needs to be stored locally now.
             *
             * Survey information lives in the database.
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
         * Ask the backend whether the user
         * already has an active 360 survey.
         */
        await loadActive360Survey();

    }
);