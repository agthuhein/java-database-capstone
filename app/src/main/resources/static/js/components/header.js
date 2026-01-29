
// Render on DOM ready (defer ensures DOM is parsed)
document.addEventListener("DOMContentLoaded", () => {
    renderHeader();
});

function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (!headerDiv) return;

    // 1) Check current page - do not show role-based header on homepage
    // If you consider "/" as homepage, also clear session
    if (window.location.pathname.endsWith("/")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        headerDiv.innerHTML = ""; // keep homepage clean
        return;
    }

    // 2) Look at role and token
    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    // 3) Handle invalid session: role implies logged-in, but token missing
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }

    // Build header HTML
    let headerContent = "";

    // Basic header shell (logo + nav placeholder)
    headerContent += `
    <header class="header">
      <div class="logo" id="logoBtn">Smart Clinic</div>
      <nav class="nav" id="navLinks">
        ${getNavLinks(role)}
      </nav>
    </header>
  `;

    // 4) Inject header
    headerDiv.innerHTML = headerContent;

    // 5) Attach listeners
    attachHeaderButtonListeners(role);
}

/**
 * Returns nav HTML based on role.
 */
function getNavLinks(role) {
    // role could be null if not set
    switch (role) {
        case "admin":
            return `
        <button id="addDocBtn" class="adminBtn" type="button">Add Doctor</button>
        <a href="#" id="logoutLink">Logout</a>
      `;

        case "doctor":
            return `
        <a href="#" id="doctorHomeLink">Home</a>
        <a href="#" id="logoutLink">Logout</a>
      `;

        case "patient":
            return `
        <a href="#" id="loginLink">Login</a>
        <a href="#" id="signupLink">Sign Up</a>
      `;

        case "loggedPatient":
            return `
        <a href="#" id="patientHomeLink">Home</a>
        <a href="#" id="appointmentsLink">Appointments</a>
        <a href="#" id="logoutPatientLink">Logout</a>
      `;

        default:
            // If role not known, show minimal options
            return `
        <a href="/" id="homeLink">Home</a>
      `;
    }
}

/**
 * Because header elements are injected dynamically,
 * bind event listeners after rendering.
 */
function attachHeaderButtonListeners(role) {
    // Logo click -> route based on role
    const logoBtn = document.getElementById("logoBtn");
    if (logoBtn) {
        logoBtn.addEventListener("click", () => {
            // basic routing suggestion
            if (role === "admin") window.location.href = "/admin/adminDashboard";
            else if (role === "doctor") window.location.href = "/doctor/doctorDashboard";
            else window.location.href = "/pages/patientDashboard.html";
        });
    }

    // Admin: Add Doctor button
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn) {
        addDocBtn.addEventListener("click", () => {
            // openModal is expected to exist (from your modals/service file)
            if (typeof window.openModal === "function") {
                window.openModal("addDoctor");
            } else {
                // fallback: trigger a custom event so adminDashboard.js can open modal
                window.dispatchEvent(new CustomEvent("modal:open", { detail: { type: "addDoctor" } }));
            }
        });
    }

    // Common logout (admin/doctor)
    const logoutLink = document.getElementById("logoutLink");
    if (logoutLink) {
        logoutLink.addEventListener("click", (e) => {
            e.preventDefault();
            logout();
        });
    }

    // Doctor Home
    const doctorHomeLink = document.getElementById("doctorHomeLink");
    if (doctorHomeLink) {
        doctorHomeLink.addEventListener("click", (e) => {
            e.preventDefault();
            window.location.href = "/doctor/doctorDashboard";
        });
    }

    // Patient (not logged): Login / Signup
    const loginLink = document.getElementById("loginLink");
    if (loginLink) {
        loginLink.addEventListener("click", (e) => {
            e.preventDefault();
            if (typeof window.openModal === "function") {
                window.openModal("login");
            } else {
                window.dispatchEvent(new CustomEvent("modal:open", { detail: { type: "login" } }));
            }
        });
    }

    const signupLink = document.getElementById("signupLink");
    if (signupLink) {
        signupLink.addEventListener("click", (e) => {
            e.preventDefault();
            if (typeof window.openModal === "function") {
                window.openModal("signup");
            } else {
                window.dispatchEvent(new CustomEvent("modal:open", { detail: { type: "signup" } }));
            }
        });
    }

    // Logged Patient: Home / Appointments / Logout
    const patientHomeLink = document.getElementById("patientHomeLink");
    if (patientHomeLink) {
        patientHomeLink.addEventListener("click", (e) => {
            e.preventDefault();
            window.location.href = "/pages/patientDashboard.html";
        });
    }

    const appointmentsLink = document.getElementById("appointmentsLink");
    if (appointmentsLink) {
        appointmentsLink.addEventListener("click", (e) => {
            e.preventDefault();
            // Update if you have a dedicated appointments page
            window.location.href = "/pages/patientDashboard.html#appointments";
        });
    }

    const logoutPatientLink = document.getElementById("logoutPatientLink");
    if (logoutPatientLink) {
        logoutPatientLink.addEventListener("click", (e) => {
            e.preventDefault();
            logoutPatient();
        });
    }
}

/**
 * Logout for admin/doctor/general.
 * Clears session and returns to homepage.
 */
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
}

/**
 * Logout for patient:
 * remove token, keep userRole as "patient" so login/signup show again,
 * then redirect to patient dashboard.
 */
function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/pages/patientDashboard.html";
}

// Make functions available if needed by inline onclicks elsewhere
window.renderHeader = renderHeader;
window.logout = logout;
window.logoutPatient = logoutPatient;