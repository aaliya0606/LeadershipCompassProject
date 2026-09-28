const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (!token) {
    window.location.href = "index.html";
}

if (role === "ADMIN") {
    document.getElementById("adminSection")?.classList.remove("d-none");
}

document.getElementById("surveyBtn")?.addEventListener("click", () => {
    window.location.href = "survey.html";
});

document.getElementById("logoutBtn")?.addEventListener("click", () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "index.html";
});