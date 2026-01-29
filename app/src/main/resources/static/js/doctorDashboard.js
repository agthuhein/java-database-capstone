// app/src/main/resources/static/js/services/doctorDashboard.js

import { getAllAppointments } from "./appointmentRecordService.js";
import { createPatientRow } from "../components/patientRows.js";

let patientTableBody;
let selectedDate;
let token;
let patientName = null;

document.addEventListener("DOMContentLoaded", () => {
    // Optional if you use renderContent() for header/footer
    if (typeof window.renderContent === "function") {
        window.renderContent();
    }

    patientTableBody = document.getElementById("patientTableBody");
    token = localStorage.getItem("token");

    // default date = today (YYYY-MM-DD)
    selectedDate = new Date().toISOString().split("T")[0];

    // Bind Search Bar
    const searchBar = document.getElementById("searchBar");
    if (searchBar) {
        searchBar.addEventListener("input", (e) => {
            const val = e.target.value.trim();
            patientName = val.length === 0 ? "null" : val;
            loadAppointments();
        });
    }

    // Today button
    const todayBtn = document.getElementById("todayAppointmentsBtn") || document.getElementById("todayButton");
    if (todayBtn) {
        todayBtn.addEventListener("click", () => {
            selectedDate = new Date().toISOString().split("T")[0];

            const datePicker = document.getElementById("appointmentDate") || document.getElementById("datePicker");
            if (datePicker) datePicker.value = selectedDate;

            loadAppointments();
        });
    }

    // Date picker
    const datePicker = document.getElementById("appointmentDate") || document.getElementById("datePicker");
    if (datePicker) {
        // initialize with today
        datePicker.value = selectedDate;

        datePicker.addEventListener("change", (e) => {
            selectedDate = e.target.value;
            loadAppointments();
        });
    }

    // Initial load
    loadAppointments();
});

async function loadAppointments() {
    if (!patientTableBody) return;

    // Clear table
    patientTableBody.innerHTML = "";

    try {
        const appointments = await getAllAppointments(selectedDate, patientName, token);

        if (!appointments || appointments.length === 0) {
            patientTableBody.innerHTML = `
        <tr>
          <td colspan="5" class="noPatientRecord">No Appointments found for today</td>
        </tr>
      `;
            return;
        }

        appointments.forEach((appt) => {
            // Expect appt.patient exists; otherwise fallback
            const patient = appt.patient || appt.patientDetails || {};

            const row = createPatientRow(appt, patient);
            patientTableBody.appendChild(row);
        });
    } catch (err) {
        console.error("loadAppointments error:", err);
        patientTableBody.innerHTML = `
      <tr>
        <td colspan="5" class="noPatientRecord">Error loading appointments. Please try again.</td>
      </tr>
    `;
    }
}
