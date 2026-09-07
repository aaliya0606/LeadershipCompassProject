const API_BASE_URL = "http://localhost:8080";

const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

const logoutBtn = document.getElementById("logoutBtn");
const surveyBtn = document.getElementById("surveyBtn");
const adminSection = document.getElementById("adminSection");

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
 * Keep track of the current survey
 * while the user is on the dashboard.
 */
let current360SurveyId = null;
let current360SurveyToken = null;


/* =========================================================
   AUTHENTICATION
   ========================================================= */

if (!token) {

    window.location.href = "index.html";

}


/* =========================================================
   ADMIN SECTION
   ========================================================= */

if (role === "ADMIN" && adminSection) {

    adminSection.classList.remove("d-none");

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

        generate360Btn.disabled = true;
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
            "360 survey created:",
            data
        );


        current360SurveyId =
            data.id;

        current360SurveyToken =
            data.token;


        /*
         * Store locally so the dashboard can
         * restore the survey after page refresh.
         *
         * Later we can replace this with a
         * backend endpoint that finds the
         * user's active survey.
         */
        localStorage.setItem(
            "current360SurveyId",
            data.id
        );

        localStorage.setItem(
            "current360SurveyToken",
            data.token
        );


        /*
         * Construct reviewer survey URL.
         */

        const link =
            `${window.location.origin}` +
            `/frontend/360-feedback.html` +
            `?token=${data.token}`;


        localStorage.setItem(
            "current360SurveyLink",
            link
        );


        showActive360Survey(
            data.id,
            data.token,
            data.status,
            link
        );


        /*
         * Load response count.
         */
        await load360ResultsSummary();


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
   DISPLAY ACTIVE SURVEY
   ========================================================= */

function showActive360Survey(
    id,
    surveyToken,
    status,
    link
) {

    current360SurveyId =
        Number(id);

    current360SurveyToken =
        surveyToken;


    /*
     * Hide create state.
     */

    if (no360SurveyState) {

        no360SurveyState.style.display =
            "none";

    }


    /*
     * Show active state.
     */

    if (active360SurveyState) {

        active360SurveyState.style.display =
            "block";

    }


    /*
     * Survey ID
     */

    if (surveyIdDisplay) {

        surveyIdDisplay.textContent =
            `#${id}`;

    }


    /*
     * Survey status
     */

    if (surveyStatus) {

        surveyStatus.textContent =
            status || "ACTIVE";

    }


    if (surveyStatusBadge) {

        surveyStatusBadge.style.display =
            "inline-block";

        surveyStatusBadge.textContent =
            status || "ACTIVE";


        if (status === "ACTIVE") {

            surveyStatusBadge.className =
                "badge bg-success";

        } else {

            surveyStatusBadge.className =
                "badge bg-secondary";

        }

    }


    /*
     * Reviewer link
     */

    if (surveyLink) {

        surveyLink.value =
            link;

    }

}


/* =========================================================
   RESTORE EXISTING SURVEY FROM LOCAL STORAGE
   ========================================================= */

function restore360Survey() {

    const storedId =
        localStorage.getItem(
            "current360SurveyId"
        );

    const storedToken =
        localStorage.getItem(
            "current360SurveyToken"
        );

    const storedLink =
        localStorage.getItem(
            "current360SurveyLink"
        );


    if (
        storedId &&
        storedToken &&
        storedLink
    ) {

        showActive360Survey(
            storedId,
            storedToken,
            "ACTIVE",
            storedLink
        );


        load360ResultsSummary();

    }

}


/* =========================================================
   LOAD RESPONSE COUNT
   ========================================================= */

async function load360ResultsSummary() {

    if (!current360SurveyId) {

        return;

    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}` +
                `/api/360/surveys/` +
                `${current360SurveyId}/results`,
                {
                    method: "GET",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        if (!response.ok) {

            console.warn(
                "Unable to load 360 summary:",
                response.status
            );

            return;

        }


        const results =
            await response.json();


        console.log(
            "360 summary:",
            results
        );


        if (responseCount) {

            responseCount.textContent =
                results.responseCount ?? 0;

        }


    } catch (error) {

        console.error(
            "Failed to load 360 response count:",
            error
        );

    }

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
            "Unable to copy link:",
            error
        );


        /*
         * Basic fallback.
         */

        surveyLink.select();

        document.execCommand("copy");


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
                `360-results.html?surveyId=` +
                current360SurveyId;

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

            localStorage.removeItem(
                "token"
            );

            localStorage.removeItem(
                "role"
            );


            /*
             * You can choose whether these should
             * be removed at logout.
             *
             * For now we remove them because they
             * belong to the logged-in session.
             */
            localStorage.removeItem(
                "current360SurveyId"
            );

            localStorage.removeItem(
                "current360SurveyToken"
            );

            localStorage.removeItem(
                "current360SurveyLink"
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
    () => {

        restore360Survey();

    }
);