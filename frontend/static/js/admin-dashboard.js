const dashboardData = {

    totalUsers: 120,

    completionRate: 84.2,

    averageScore: 72.6,

    planCompletion: 68.5,


    leadershipProfiles: {
        "Caring Time": 32,
        "Words of Recognition": 24,
        "Psychological Touch": 41,
        "Acts of Support": 23,
        "Receiving Value": 30
    },

    skillGaps: {
        "Communication": 42,
        "Delegation": 35,
        "Strategic Thinking": 31,
        "Feedback": 27,
        "Emotional Intelligence": 21
    },

    progress: {
        labels: [
            "Initial Assessment",
            "Week 1",
            "Week 2",
            "Week 3",
            "Week 4",
            "Week 5"
        ],

        scores: [
            68,
            69,
            71,
            73,
            76,
            79
        ]
    }
};
document.getElementById("totalUsers").textContent =
    dashboardData.totalUsers;

document.getElementById("completionRate").textContent =
    dashboardData.completionRate + "%";

document.getElementById("averageScore").textContent =
    dashboardData.averageScore;

document.getElementById("planCompletion").textContent =
    dashboardData.planCompletion + "%";

new Chart(
    document.getElementById("leadershipProfileChart"),
    {
        type: "doughnut",

        data: {
            labels: Object.keys(
                dashboardData.leadershipProfiles
            ),

            datasets: [{
                data: Object.values(
                    dashboardData.leadershipProfiles
                )
            }]
        },

        options: {
            responsive: true,

            plugins: {
                legend: {
                    position: "bottom"
                }
            }
        }
    }
);

new Chart(
    document.getElementById("skillGapChart"),
    {
        type: "bar",

        data: {
            labels: Object.keys(
                dashboardData.skillGaps
            ),

            datasets: [{
                label: "Participants",
                data: Object.values(
                    dashboardData.skillGaps
                )
            }]
        },

        options: {
            responsive: true,

            scales: {
                y: {
                    beginAtZero: true
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

new Chart(
    document.getElementById("progressChart"),
    {
        type: "line",

        data: {
            labels: dashboardData.progress.labels,

            datasets: [{
                label: "Average Leadership Score",

                data: dashboardData.progress.scores,

                tension: 0.3,

                fill: false
            }]
        },

        options: {
            responsive: true,

            scales: {
                y: {
                    beginAtZero: false,
                    min: 40,
                    max: 100
                }
            }
        }
    }
);