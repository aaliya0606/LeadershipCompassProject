// ============================================================
// ADMIN DASHBOARD - DUMMY DATA
// ============================================================
// This data is structured to resemble the data that will
// eventually come from the Spring Boot backend / PostgreSQL.
//
// Users
// Survey Results
// Development Plans
// ============================================================


const dashboardData = {

    // ========================================================
    // USERS
    // ========================================================

    users: [

        {
            id: 1,
            fullName: "Alex Johnson",
            department: "IT",
            role: "USER"
        },

        {
            id: 2,
            fullName: "Sarah Williams",
            department: "IT",
            role: "USER"
        },

        {
            id: 3,
            fullName: "James Smith",
            department: "Sales",
            role: "USER"
        },

        {
            id: 4,
            fullName: "Emily Brown",
            department: "Sales",
            role: "USER"
        },

        {
            id: 5,
            fullName: "Michael Jones",
            department: "Executive",
            role: "USER"
        },

        {
            id: 6,
            fullName: "Olivia Davis",
            department: "Executive",
            role: "USER"
        },

        {
            id: 7,
            fullName: "Daniel Wilson",
            department: "IT",
            role: "USER"
        },

        {
            id: 8,
            fullName: "Sophie Taylor",
            department: "Sales",
            role: "USER"
        }
    ],


    // ========================================================
    // SURVEY RESULTS
    // ========================================================
    // Each result belongs to a user through userId.
    //
    // Each Leadership Compass area is scored out of 50.
    // Overall score is out of 250.
    //
    // User 8 has not completed the assessment yet.
    // ========================================================

    surveyResults: [

        {
            resultId: 1,
            userId: 1,

            caringTimeScore: 38,
            receivingValueScore: 42,
            actsOfSupportScore: 35,
            wordsOfRecognitionScore: 40,
            psychologicalTouchScore: 37,

            overallScore: 192,
            scoreBand: "Strong intent",

            generateDate: "2026-07-01"
        },

        {
            resultId: 2,
            userId: 2,

            caringTimeScore: 32,
            receivingValueScore: 36,
            actsOfSupportScore: 31,
            wordsOfRecognitionScore: 34,
            psychologicalTouchScore: 30,

            overallScore: 163,
            scoreBand: "Strong intent",

            generateDate: "2026-07-02"
        },

        {
            resultId: 3,
            userId: 3,

            caringTimeScore: 28,
            receivingValueScore: 34,
            actsOfSupportScore: 30,
            wordsOfRecognitionScore: 36,
            psychologicalTouchScore: 29,

            overallScore: 157,
            scoreBand: "Strong intent",

            generateDate: "2026-07-03"
        },

        {
            resultId: 4,
            userId: 4,

            caringTimeScore: 35,
            receivingValueScore: 39,
            actsOfSupportScore: 33,
            wordsOfRecognitionScore: 38,
            psychologicalTouchScore: 34,

            overallScore: 179,
            scoreBand: "Strong intent",

            generateDate: "2026-07-04"
        },

        {
            resultId: 5,
            userId: 5,

            caringTimeScore: 42,
            receivingValueScore: 44,
            actsOfSupportScore: 40,
            wordsOfRecognitionScore: 43,
            psychologicalTouchScore: 41,

            overallScore: 210,
            scoreBand: "High",

            generateDate: "2026-07-05"
        },

        {
            resultId: 6,
            userId: 6,

            caringTimeScore: 39,
            receivingValueScore: 41,
            actsOfSupportScore: 37,
            wordsOfRecognitionScore: 40,
            psychologicalTouchScore: 38,

            overallScore: 195,
            scoreBand: "Strong intent",

            generateDate: "2026-07-06"
        },

        {
            resultId: 7,
            userId: 7,

            caringTimeScore: 30,
            receivingValueScore: 33,
            actsOfSupportScore: 29,
            wordsOfRecognitionScore: 31,
            psychologicalTouchScore: 28,

            overallScore: 151,
            scoreBand: "Strong intent",

            generateDate: "2026-07-07"
        }

    ],


    // ========================================================
    // DEVELOPMENT PLANS
    // ========================================================
    //
    // Development plan completion is calculated from:
    //
    // completedTasks / totalTasks * 100
    //
    // ========================================================

    developmentPlans: [

        {
            planId: 1,
            userId: 1,
            completedTasks: 8,
            totalTasks: 10
        },

        {
            planId: 2,
            userId: 2,
            completedTasks: 7,
            totalTasks: 10
        },

        {
            planId: 3,
            userId: 3,
            completedTasks: 5,
            totalTasks: 10
        },

        {
            planId: 4,
            userId: 4,
            completedTasks: 10,
            totalTasks: 10
        },

        {
            planId: 5,
            userId: 5,
            completedTasks: 9,
            totalTasks: 10
        },

        {
            planId: 6,
            userId: 6,
            completedTasks: 6,
            totalTasks: 10
        }
    ]

};


// ============================================================
// CALCULATION FUNCTIONS
// ============================================================

//Calculates the average value for a specific field across a collection of survey results.
function calculateAverage(results, field) {

    if (results.length === 0) {
        return 0;
    }

    const total = results.reduce(
        (sum, result) => sum + result[field],
        0
    );

    return total / results.length;
}

//Gets survey results belonging in a specific department.
function getDepartmentResults(department) {

    const departmentUsers =
        dashboardData.users.filter(
            user => user.department === department
        );

    const userIds =
        departmentUsers.map(user => user.id);

    return dashboardData.surveyResults.filter(
        result => userIds.includes(result.userId)
    );
}


/**
 * Gets users belonging to a specific department.
 */
function getDepartmentUsers(department) {

    return dashboardData.users.filter(
        user => user.department === department
    );

}


/**
 * Calculates all dashboard metrics.
 */
function calculateDashboardMetrics(results, users, developmentPlans) {

    // --------------------------------------------------------
    // TOTAL USERS
    // --------------------------------------------------------

    const totalUsers = users.length;


    // --------------------------------------------------------
    // ASSESSMENT COMPLETION
    // --------------------------------------------------------

    const completedAssessments = results.length;

    const completionRate =
        totalUsers > 0
            ? (completedAssessments / totalUsers) * 100
            : 0;


    // --------------------------------------------------------
    // AVERAGE OVERALL LEADERSHIP SCORE
    // --------------------------------------------------------

    const averageScore =
        calculateAverage(
            results,
            "overallScore"
        );


    // --------------------------------------------------------
    // FIVE LEADERSHIP COMPASS AREAS
    // --------------------------------------------------------

    const leadershipProfiles = {

        "Caring Time":
            calculateAverage(
                results,
                "caringTimeScore"
            ),

        "Receiving Value":
            calculateAverage(
                results,
                "receivingValueScore"
            ),

        "Acts of Support":
            calculateAverage(
                results,
                "actsOfSupportScore"
            ),

        "Words of Recognition":
            calculateAverage(
                results,
                "wordsOfRecognitionScore"
            ),

        "Psychological Touch":
            calculateAverage(
                results,
                "psychologicalTouchScore"
            )
    };


    // --------------------------------------------------------
    // DEVELOPMENT PLAN COMPLETION
    // --------------------------------------------------------

    const totalTasks =
        developmentPlans.reduce(
            (total, plan) =>
                total + plan.totalTasks,
            0
        );


    const completedTasks =
        developmentPlans.reduce(
            (total, plan) =>
                total + plan.completedTasks,
            0
        );


    const planCompletion =
        totalTasks > 0
            ? (completedTasks / totalTasks) * 100
            : 0;


    // --------------------------------------------------------
    // RETURN DASHBOARD DATA
    // --------------------------------------------------------

    return {

        totalUsers,

        completionRate,

        averageScore,

        planCompletion,

        leadershipProfiles

    };

}


// ============================================================
// INITIAL DASHBOARD CALCULATION
// ============================================================

let currentDepartment = "All Departments";

let currentResults =
    dashboardData.surveyResults;

let currentUsers =
    dashboardData.users;

let currentDevelopmentPlans =
    dashboardData.developmentPlans;


let dashboardMetrics =
    calculateDashboardMetrics(
        currentResults,
        currentUsers,
        currentDevelopmentPlans
    );


// ============================================================
// DISPLAY DASHBOARD METRICS
// ============================================================


document.getElementById("totalUsers").textContent =
    dashboardMetrics.totalUsers;


document.getElementById("completionRate").textContent =
    dashboardMetrics.completionRate.toFixed(1) + "%";


document.getElementById("averageScore").textContent =
    dashboardMetrics.averageScore.toFixed(1);


document.getElementById("planCompletion").textContent =
    dashboardMetrics.planCompletion.toFixed(1) + "%";


// ============================================================
// LEADERSHIP AREAS CHART
// ============================================================


new Chart(
    document.getElementById("leadershipProfileChart"),
    {

        type: "bar",

        data: {

            labels:
                Object.keys(
                    dashboardMetrics.leadershipProfiles
                ),

            datasets: [

                {

                    label: "Average Score",

                    data:
                        Object.values(
                            dashboardMetrics.leadershipProfiles
                        ),

                    borderWidth: 1

                }

            ]

        },

        options: {

            responsive: true,

            scales: {

                y: {

                    beginAtZero: true,

                    max: 50,

                    title: {

                        display: true,

                        text: "Average Score / 50"

                    }

                }

            },

            plugins: {

                legend: {

                    display: false

                }

            }

        }

    }
);


// ============================================================
// SKILL GAPS
// ============================================================
//
// For now, skill gaps are based on the five Leadership
// Compass areas with the lowest average scores.
//
// Later this can be changed to use individual survey
// question averages if required.
// ============================================================


const skillGaps = {

    "Caring Time":
        dashboardMetrics.leadershipProfiles["Caring Time"],

    "Receiving Value":
        dashboardMetrics.leadershipProfiles["Receiving Value"],

    "Acts of Support":
        dashboardMetrics.leadershipProfiles["Acts of Support"],

    "Words of Recognition":
        dashboardMetrics.leadershipProfiles["Words of Recognition"],

    "Psychological Touch":
        dashboardMetrics.leadershipProfiles["Psychological Touch"]

};


// Sort areas from lowest to highest score.

const sortedSkillGaps =
    Object.entries(skillGaps)
        .sort((a, b) => a[1] - b[1])
        .slice(0, 3);


// Convert back into an object for Chart.js.

const skillGapLabels =
    sortedSkillGaps.map(
        item => item[0]
    );


const skillGapValues =
    sortedSkillGaps.map(
        item => item[1]
    );


// ============================================================
// SKILL GAP CHART
// ============================================================


new Chart(
    document.getElementById("skillGapChart"),
    {

        type: "bar",

        data: {

            labels: skillGapLabels,

            datasets: [

                {

                    label: "Average Score",

                    data: skillGapValues,

                    borderWidth: 1

                }

            ]

        },

        options: {

            responsive: true,

            scales: {

                y: {

                    beginAtZero: true,

                    max: 50,

                    title: {

                        display: true,

                        text: "Average Score / 50"

                    }

                }

            },

            plugins: {

                legend: {

                    display: false

                }

            }

        }

    }
);


// ============================================================
// PROGRESS DATA
// ============================================================
//
// This is temporary dummy data for the progress chart.
//
// Eventually this should come from multiple SurveyResult
// records taken at different points in time.
//
// Example:
// Initial assessment
// Week 1
// Week 2
// ...
// Week 5
// ============================================================


const progress = {

    labels: [

        "Initial Assessment",

        "Week 1",

        "Week 2",

        "Week 3",

        "Week 4",

        "Week 5"

    ],

    scores: [

        168,

        170,

        174,

        178,

        183,

        188

    ]

};


// ============================================================
// PROGRESS CHART
// ============================================================


new Chart(
    document.getElementById("progressChart"),
    {

        type: "line",

        data: {

            labels:
                progress.labels,

            datasets: [

                {

                    label:
                        "Average Leadership Score",

                    data:
                        progress.scores,

                    tension: 0.3,

                    fill: false,

                    borderWidth: 2

                }

            ]

        },

        options: {

            responsive: true,

            scales: {

                y: {

                    beginAtZero: false,

                    min: 100,

                    max: 250,

                    title: {

                        display: true,

                        text: "Overall Score / 250"

                    }

                }

            }

        }

    }
);


// ============================================================
// DEPARTMENT FILTER
// ============================================================
//
// If your HTML has a select with:
// id="departmentFilter"
// this will allow the dashboard to filter by department.
//
// Example options:
//
// All Departments
// IT
// Sales
// Executive
//
// ============================================================


const departmentFilter =
    document.getElementById(
        "departmentFilter"
    );


if (departmentFilter) {

    departmentFilter.addEventListener(
        "change",
        function () {

            currentDepartment =
                this.value;


            // ------------------------------------------------
            // Get users for selected department
            // ------------------------------------------------

            if (
                currentDepartment ===
                "All Departments"
            ) {

                currentUsers =
                    dashboardData.users;

                currentResults =
                    dashboardData.surveyResults;

                currentDevelopmentPlans =
                    dashboardData.developmentPlans;

            } else {

                currentUsers =
                    getDepartmentUsers(
                        currentDepartment
                    );


                currentResults =
                    getDepartmentResults(
                        currentDepartment
                    );


                const userIds =
                    currentUsers.map(
                        user => user.id
                    );


                currentDevelopmentPlans =
                    dashboardData.developmentPlans
                        .filter(
                            plan =>
                                userIds.includes(
                                    plan.userId
                                )
                        );

            }


            // ------------------------------------------------
            // Recalculate dashboard
            // ------------------------------------------------

            dashboardMetrics =
                calculateDashboardMetrics(
                    currentResults,
                    currentUsers,
                    currentDevelopmentPlans
                );


            // ------------------------------------------------
            // Update dashboard cards
            // ------------------------------------------------

            document.getElementById(
                "totalUsers"
            ).textContent =
                dashboardMetrics.totalUsers;


            document.getElementById(
                "completionRate"
            ).textContent =
                dashboardMetrics.completionRate.toFixed(1)
                + "%";


            document.getElementById(
                "averageScore"
            ).textContent =
                dashboardMetrics.averageScore.toFixed(1);


            document.getElementById(
                "planCompletion"
            ).textContent =
                dashboardMetrics.planCompletion.toFixed(1)
                + "%";


            console.log(
                "Dashboard updated for:",
                currentDepartment
            );

        }
    );

}


// ============================================================
// DEBUGGING
// ============================================================
//
// You can open the browser console to see the calculated
// dashboard values.
//
// ============================================================


console.log(
    "Dashboard Metrics:",
    dashboardMetrics
);

console.log(
    "Users:",
    dashboardData.users
);

console.log(
    "Survey Results:",
    dashboardData.surveyResults
);

console.log(
    "Development Plans:",
    dashboardData.developmentPlans
);