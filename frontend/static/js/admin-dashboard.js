// ============================================================
// ADMIN DASHBOARD
// ============================================================

const API_BASE_URL = "http://localhost:8080";

let leadershipProfileChart = null;
let skillGapChart = null;
let progressChart = null;


// ============================================================
// LOAD DASHBOARD
// ============================================================

async function loadDashboard(department = "all") {

    try {

        let url = `${API_BASE_URL}/api/admin/dashboard`;

        // Add department filter if selected
        if (department && department !== "all") {
            url += `?department=${encodeURIComponent(department)}`;
        }

        // Get JWT token
        const token = localStorage.getItem("token");

        if (!token) {

            console.error("No authentication token found.");

            window.location.href = "login.html";

            return;
        }

        console.log("Loading dashboard:", url);
        console.log("Token found:", true);


        // ----------------------------------------------------
        // API REQUEST
        // ----------------------------------------------------

        const response = await fetch(url, {

            method: "GET",

            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }

        });


        console.log(
            "Dashboard response status:",
            response.status
        );


        // ----------------------------------------------------
        // HANDLE ERROR
        // ----------------------------------------------------

        if (!response.ok) {

            const errorText = await response.text();

            console.error(
                "Backend error response:",
                errorText
            );

            throw new Error(
                `Dashboard request failed: ${response.status} ${response.statusText}`
            );
        }


        // ----------------------------------------------------
        // READ RESPONSE
        // ----------------------------------------------------

        const data = await response.json();

        console.log(
            "Dashboard data received:",
            data
        );


        // ----------------------------------------------------
        // UPDATE DASHBOARD
        // ----------------------------------------------------

        updateDashboard(data);

    }

    catch (error) {

        console.error(
            "Failed to load dashboard:",
            error
        );

        showDashboardError();

    }

}


// ============================================================
// UPDATE DASHBOARD
// ============================================================

function updateDashboard(data) {

    console.log(
        "Updating dashboard with:",
        data
    );


    // ========================================================
    // KPI CARDS
    // ========================================================

    /*
     * These names match the fields currently being returned
     * by your AdminDashboardResponse.
     */

    setText(
        "totalUsers",
        data.totalUsers ?? 0
    );


    setText(
        "completionRate",
        formatPercentage(
            data.assessmentCompletionRate
        )
    );


    setText(
        "averageScore",
        formatNumber(
            data.averageLeadershipScore
        )
    );


    /*
     * Development plan completion is not currently shown
     * in your backend response.
     *
     * This will display 0% until you add the calculation.
     */

    setText(
        "planCompletion",
        formatPercentage(
            data.planCompletion
        )
    );


    // ========================================================
    // LEADERSHIP PROFILE
    // ========================================================

    updateLeadershipProfileChart(data);


    // ========================================================
    // SKILL GAPS
    // ========================================================

    updateSkillGapChart(data);


    // ========================================================
    // PROGRESS
    // ========================================================

    updateProgressChart(data);

}


// ============================================================
// TEXT HELPER
// ============================================================

function setText(elementId, value) {

    const element =
        document.getElementById(elementId);

    if (!element) {

        console.warn(
            `Element not found: ${elementId}`
        );

        return;
    }

    element.textContent = value;

}


// ============================================================
// FORMAT PERCENTAGE
// ============================================================

function formatPercentage(value) {

    if (
        value === null ||
        value === undefined ||
        isNaN(value)
    ) {

        return "0%";
    }

    return `${Number(value).toFixed(1)}%`;

}


// ============================================================
// FORMAT NUMBER
// ============================================================

function formatNumber(value) {

    if (
        value === null ||
        value === undefined ||
        isNaN(value)
    ) {

        return "0";
    }

    return Number(value).toFixed(1);

}


// ============================================================
// LEADERSHIP PROFILE CHART
// ============================================================

function updateLeadershipProfileChart(data) {

    const canvas =
        document.getElementById(
            "leadershipProfileChart"
        );


    if (!canvas) {

        console.warn(
            "leadershipProfileChart canvas not found."
        );

        return;
    }


    if (typeof Chart === "undefined") {

        console.error(
            "Chart.js has not loaded."
        );

        return;
    }


    const ctx =
        canvas.getContext("2d");


    // Destroy existing chart

    if (leadershipProfileChart) {

        leadershipProfileChart.destroy();

    }


    // --------------------------------------------------------
    // LEADERSHIP AREAS
    // --------------------------------------------------------

    const labels = [

        "Caring Time",

        "Receiving Value",

        "Acts of Support",

        "Words of Recognition",

        "Psychological Touch"

    ];


    // --------------------------------------------------------
    // GET REAL SCORES FROM BACKEND
    // --------------------------------------------------------

    const values = [

        data.averageCaringTimeScore ?? 0,

        data.averageReceivingValueScore ?? 0,

        data.averageActsOfSupportScore ?? 0,

        data.averageWordsOfRecognitionScore ?? 0,

        data.averagePsychologicalTouchScore ?? 0

    ];


    console.log(
        "Leadership profile scores:",
        values
    );


    // --------------------------------------------------------
    // CREATE CHART
    // --------------------------------------------------------

    leadershipProfileChart =
        new Chart(ctx, {

            type: "bar",

            data: {

                labels: labels,

                datasets: [

                    {

                        label:
                            "Average Score",

                        data:
                            values,

                        borderWidth: 1

                    }

                ]

            },


            options: {

                responsive: true,

                maintainAspectRatio: false,


                plugins: {

                    legend: {

                        display: false

                    }

                },


                scales: {

                    y: {

                        beginAtZero: true,

                        max: 50,

                        ticks: {

                            precision: 0

                        }

                    }

                }

            }

        });

}


// ============================================================
// SKILL GAP CHART
// ============================================================

function updateSkillGapChart(data) {

    const canvas =
        document.getElementById(
            "skillGapChart"
        );


    if (!canvas) {

        console.warn(
            "skillGapChart canvas not found."
        );

        return;
    }


    if (typeof Chart === "undefined") {

        console.error(
            "Chart.js has not loaded."
        );

        return;
    }


    const ctx =
        canvas.getContext("2d");


    if (skillGapChart) {

        skillGapChart.destroy();

    }


    /*
     * Currently your backend returns:
     *
     * skillGaps: {}
     *
     * Therefore we temporarily calculate the skill gaps
     * from the five leadership scores.
     *
     * Lower score = larger development gap.
     */


    const skillGaps = {

        "Caring Time":
            data.averageCaringTimeScore ?? 0,

        "Receiving Value":
            data.averageReceivingValueScore ?? 0,

        "Acts of Support":
            data.averageActsOfSupportScore ?? 0,

        "Words of Recognition":
            data.averageWordsOfRecognitionScore ?? 0,

        "Psychological Touch":
            data.averagePsychologicalTouchScore ?? 0

    };


    // Sort lowest score first

    const sorted =
        Object.entries(skillGaps)
            .sort((a, b) => a[1] - b[1]);


    const labels =
        sorted.map(item => item[0]);


    const scores =
        sorted.map(item => item[1]);


    console.log(
        "Skill gaps:",
        skillGaps
    );


    skillGapChart =
        new Chart(ctx, {

            type: "bar",

            data: {

                labels: labels,

                datasets: [

                    {

                        label:
                            "Average Score",

                        data:
                            scores,

                        borderWidth: 1

                    }

                ]

            },


            options: {

                indexAxis: "y",

                responsive: true,

                maintainAspectRatio: false,


                plugins: {

                    legend: {

                        display: false

                    }

                },


                scales: {

                    x: {

                        beginAtZero: true,

                        max: 50,

                        ticks: {

                            precision: 0

                        }

                    }

                }

            }

        });

}


// ============================================================
// PROGRESS CHART
// ============================================================

function updateProgressChart(data) {

    const canvas =
        document.getElementById(
            "progressChart"
        );


    if (!canvas) {

        console.warn(
            "progressChart canvas not found."
        );

        return;
    }


    if (typeof Chart === "undefined") {

        console.error(
            "Chart.js has not loaded."
        );

        return;
    }


    const ctx =
        canvas.getContext("2d");


    if (progressChart) {

        progressChart.destroy();

    }


    /*
     * Your backend does not currently return historical
     * progress data.
     *
     * Therefore we use the current average score as the
     * initial assessment point.
     */


    const labels = [

        "Current Assessment"

    ];


    const scores = [

        data.averageLeadershipScore ?? 0

    ];


    console.log(
        "Progress:",
        {
            labels: labels,
            scores: scores
        }
    );


    progressChart =
        new Chart(ctx, {

            type: "line",

            data: {

                labels: labels,

                datasets: [

                    {

                        label:
                            "Average Leadership Score",

                        data:
                            scores,

                        fill: false,

                        tension: 0.3,

                        borderWidth: 2,

                        pointRadius: 5

                    }

                ]

            },


            options: {

                responsive: true,

                maintainAspectRatio: false,


                plugins: {

                    legend: {

                        display: true

                    }

                },


                scales: {

                    y: {

                        beginAtZero: true,

                        max: 250

                    }

                }

            }

        });

}


// ============================================================
// ERROR HANDLING
// ============================================================

function showDashboardError() {

    setText(
        "totalUsers",
        "--"
    );

    setText(
        "completionRate",
        "--"
    );

    setText(
        "averageScore",
        "--"
    );

    setText(
        "planCompletion",
        "--"
    );

}


// ============================================================
// DEPARTMENT FILTER
// ============================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {


        console.log(
            "Admin dashboard loaded."
        );


        // ----------------------------------------------------
        // Department dropdown
        // ----------------------------------------------------

        const departmentFilter =
            document.getElementById(
                "departmentFilter"
            );


        if (departmentFilter) {

            departmentFilter.addEventListener(
                "change",
                function () {

                    const department =
                        departmentFilter.value;


                    console.log(
                        "Department selected:",
                        department
                    );


                    loadDashboard(
                        department
                    );

                }
            );

        }


        // ----------------------------------------------------
        // Initial dashboard load
        // ----------------------------------------------------

        loadDashboard("all");

    }
);