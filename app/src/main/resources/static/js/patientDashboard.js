// app/src/main/resources/static/js/patientDashboard.js

import { createDoctorCard } from "./components/doctorCard.js";
import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors } from "./services/doctorServices.js";
import { patientLogin, patientSignup } from "./services/patientServices.js";

document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();

  // Bind modal triggers (these IDs are expected to exist in header rendering for patient role)
  const signupBtn = document.getElementById("patientSignup");
  if (signupBtn) signupBtn.addEventListener("click", () => openModal("patientSignup"));

  const loginBtn = document.getElementById("patientLogin");
  if (loginBtn) loginBtn.addEventListener("click", () => openModal("patientLogin"));

  // Search + filters
  const searchBar = document.getElementById("searchBar");
  if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);

  const filterTime = document.getElementById("filterTime");
  if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);

  const filterSpecialty = document.getElementById("filterSpecialty");
  if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
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
  const time = (document.getElementById("filterTime")?.value || "").trim();
  const specialty = (document.getElementById("filterSpecialty")?.value || "").trim();

  const doctors = await filterDoctors(name, time, specialty);

  if (doctors && doctors.length > 0) {
    renderDoctorCards(doctors);
  } else {
    contentDiv.innerHTML = `<p class="noPatientRecord">No doctors found with the given filters.</p>`;
  }
}

/**
 * Signup form handler (modal submit)
 * Make global so modal form button can call signupPatient()
 */
window.signupPatient = async function signupPatient() {
  try {
    const name = document.getElementById("patientName")?.value?.trim();
    const email = document.getElementById("patientEmail")?.value?.trim();
    const password = document.getElementById("patientPassword")?.value?.trim();
    const phone = document.getElementById("patientPhone")?.value?.trim();
    const address = document.getElementById("patientAddress")?.value?.trim();

    if (!name || !email || !password || !phone || !address) {
      alert("Please fill in all fields.");
      return;
    }

    const payload = { name, email, password, phone, address };

    const result = await patientSignup(payload);

    if (result.success) {
      alert(result.message || "Signup successful!");
      if (typeof window.closeModal === "function") window.closeModal();
      window.location.reload();
    } else {
      alert(result.message || "Signup failed.");
    }
  } catch (err) {
    console.error("signupPatient error:", err);
    alert("Unexpected error during signup.");
  }
};

/**
 * Login form handler (modal submit)
 * Make global so modal form button can call loginPatient()
 */
window.loginPatient = async function loginPatient() {
  try {
    const email = document.getElementById("loginEmail")?.value?.trim();
    const password = document.getElementById("loginPassword")?.value?.trim();

    if (!email || !password) {
      alert("Please enter email and password.");
      return;
    }

    const res = await patientLogin({ email, password });

    if (!res) {
      alert("Server not reachable. Please try again.");
      return;
    }

    if (!res.ok) {
      alert("Invalid credentials!");
      return;
    }

    const data = await res.json().catch(() => ({}));
    const token = data?.token ?? data;

    if (!token) {
      alert("Login failed: token missing.");
      return;
    }

    localStorage.setItem("token", token);
    localStorage.setItem("userRole", "loggedPatient");

    if (typeof window.closeModal === "function") window.closeModal();

    // Redirect to patient dashboard (logged patient state is handled by header.js)
    window.location.href = "/pages/patientDashboard.html";
  } catch (err) {
    console.error("loginPatient error:", err);
    alert("Unexpected error during login.");
  }
};
