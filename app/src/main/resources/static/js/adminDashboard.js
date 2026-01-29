// app/src/main/resources/static/js/services/adminDashboard.js

import { openModal } from "../components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./doctorServices.js";
import { createDoctorCard } from "../components/doctorCard.js";

document.addEventListener("DOMContentLoaded", () => {
    // Add Doctor button (comes from header.js)
    const addBtn = document.getElementById("addDocBtn");
    if (addBtn) {
        addBtn.addEventListener("click", () => openModal("addDoctor"));
    }

    // Search + filters
    const searchBar = document.getElementById("searchBar");
    if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);

    const timeSelect = document.getElementById("filterTime") || document.getElementById("sortByTime");
    if (timeSelect) timeSelect.addEventListener("change", filterDoctorsOnChange);

    const specialtySelect = document.getElementById("filterSpecialty") || document.getElementById("filterBySpecialty");
    if (specialtySelect) specialtySelect.addEventListener("change", filterDoctorsOnChange);

    // Initial load
    loadDoctorCards();
});

async function loadDoctorCards() {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;

    contentDiv.innerHTML = "";

    const doctors = await getDoctors();
    renderDoctorCards(doctors);
}

function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;

    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = `<p class="noPatientRecord">No doctors found</p>`;
        return;
    }

    doctors.forEach((doc) => {
        const card = createDoctorCard(doc);
        contentDiv.appendChild(card);
    });
}

async function filterDoctorsOnChange() {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;

    const name = (document.getElementById("searchBar")?.value || "").trim();

    // Support either id set depending on your HTML (adminDashboard.html variants)
    const time =
        (document.getElementById("filterTime")?.value ||
            document.getElementById("sortByTime")?.value ||
            "").trim();

    const specialty =
        (document.getElementById("filterSpecialty")?.value ||
            document.getElementById("filterBySpecialty")?.value ||
            "").trim();

    const doctors = await filterDoctors(name, time, specialty);
    renderDoctorCards(doctors);
}

/**
 * Add Doctor from modal form submission
 * Make it global so modal HTML can call: onclick="adminAddDoctor()"
 */
window.adminAddDoctor = async function adminAddDoctor() {
    try {
        const token = localStorage.getItem("token");
        if (!token) {
            alert("Session expired. Please login again.");
            window.location.href = "/";
            return;
        }

        // These IDs should match inputs created inside openModal('addDoctor')
        const name = document.getElementById("docName")?.value?.trim();
        const specialty = document.getElementById("docSpecialty")?.value?.trim();
        const email = document.getElementById("docEmail")?.value?.trim();
        const password = document.getElementById("docPassword")?.value?.trim();
        const phone = document.getElementById("docPhone")?.value?.trim();

        // Availability could be checkboxes OR a comma-separated input
        // Option A: checkboxes name="availability"
        const checked = Array.from(document.querySelectorAll('input[name="availability"]:checked')).map(
            (c) => c.value
        );

        // Option B: text input id="docAvailableTimes" => "09:00,10:00"
        const timesText = document.getElementById("docAvailableTimes")?.value?.trim();
        const fromText = timesText ? timesText.split(",").map((t) => t.trim()).filter(Boolean) : [];

        const availableTimes = checked.length > 0 ? checked : fromText;

        if (!name || !specialty || !email || !password || !phone) {
            alert("Please fill in all required fields.");
            return;
        }

        const doctor = {
            name,
            specialty,
            email,
            password,
            phone,
            availableTimes,
        };

        const result = await saveDoctor(doctor, token);

        if (result.success) {
            alert(result.message || "Doctor added successfully!");

            // Close modal if your modal system supports it
            if (typeof window.closeModal === "function") window.closeModal();

            // Reload doctor list
            await loadDoctorCards();
        } else {
            alert(result.message || "Failed to add doctor.");
        }
    } catch (err) {
        console.error("adminAddDoctor error:", err);
        alert("Unexpected error while adding doctor.");
    }
};
