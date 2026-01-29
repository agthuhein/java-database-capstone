// app/src/main/resources/static/js/services/index.js

import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = API_BASE_URL + "/admin";
const DOCTOR_API = API_BASE_URL + "/doctor/login";

// Attach click listeners after page loads
window.onload = function () {
    const adminBtn = document.getElementById("adminLogin");
    if (adminBtn) {
        adminBtn.addEventListener("click", () => {
            openModal("adminLogin");
        });
    }

    const doctorBtn = document.getElementById("doctorLogin");
    if (doctorBtn) {
        doctorBtn.addEventListener("click", () => {
            openModal("doctorLogin");
        });
    }
};

/**
 * Admin login handler
 * Reads username/password from modal inputs and authenticates.
 * Exposed globally so it can be called from modal HTML (onclick / submit).
 */
async function adminLoginHandler() {
    try {
        const usernameEl = document.getElementById("adminUsername");
        const passwordEl = document.getElementById("adminPassword");

        const username = usernameEl ? usernameEl.value.trim() : "";
        const password = passwordEl ? passwordEl.value.trim() : "";

        if (!username || !password) {
            alert("Please enter username and password.");
            return;
        }

        const admin = { username, password };

        const res = await fetch(ADMIN_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(admin),
        });

        if (!res.ok) {
            alert("Invalid credentials!");
            return;
        }

        const data = await res.json();

        // Assumption: backend returns { token: "..." } or a raw token string
        const token = data?.token ?? data;

        if (!token) {
            alert("Login failed: token not found in response.");
            return;
        }

        localStorage.setItem("token", token);

        // selectRole should exist in render.js and store role + redirect/render
        if (typeof window.selectRole === "function") {
            window.selectRole("admin");
        } else {
            localStorage.setItem("userRole", "admin");
            window.location.href = "/admin/adminDashboard";
        }
    } catch (err) {
        console.error("Admin login error:", err);
        alert("Something went wrong. Please try again.");
    }
}

/**
 * Doctor login handler
 * Reads email/password from modal inputs and authenticates.
 * Exposed globally so it can be called from modal HTML (onclick / submit).
 */
async function doctorLoginHandler() {
    try {
        const emailEl = document.getElementById("doctorEmail");
        const passwordEl = document.getElementById("doctorPassword");

        const email = emailEl ? emailEl.value.trim() : "";
        const password = passwordEl ? passwordEl.value.trim() : "";

        if (!email || !password) {
            alert("Please enter email and password.");
            return;
        }

        const doctor = { email, password };

        const res = await fetch(DOCTOR_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(doctor),
        });

        if (!res.ok) {
            alert("Invalid credentials!");
            return;
        }

        const data = await res.json();
        const token = data?.token ?? data;

        if (!token) {
            alert("Login failed: token not found in response.");
            return;
        }

        localStorage.setItem("token", token);

        if (typeof window.selectRole === "function") {
            window.selectRole("doctor");
        } else {
            localStorage.setItem("userRole", "doctor");
            window.location.href = "/doctor/doctorDashboard";
        }
    } catch (err) {
        console.error("Doctor login error:", err);
        alert("Something went wrong. Please try again.");
    }
}

// Make handlers accessible globally (
