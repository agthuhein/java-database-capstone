// app/src/main/resources/static/js/services/patientServices.js

import { API_BASE_URL } from "../config/config.js";

const PATIENT_API = API_BASE_URL + "/patient";

export async function patientSignup(data) {
  try {
    const res = await fetch(`${PATIENT_API}/signup`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });

    const body = await res.json().catch(() => ({}));

    if (!res.ok) {
      return {
        success: false,
        message: body?.message || "Signup failed. Please try again.",
      };
    }

    return {
      success: true,
      message: body?.message || "Signup successful.",
    };
  } catch (err) {
    console.error("patientSignup error:", err);
    return {
      success: false,
      message: "Network/server error during signup.",
    };
  }
}

/**
 * Patient Login
 * POST {PATIENT_API}/login
 * Returns the full fetch Response so UI can check status and extract token.
 * @param {Object} data - { email, password }
 * @returns {Promise<Response|null>}
 */
export async function patientLogin(data) {
  try {
    // helpful during development; remove in production if needed
    console.log("patientLogin payload:", data);

    const res = await fetch(`${PATIENT_API}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });

    return res;
  } catch (err) {
    console.error("patientLogin error:", err);
    return null;
  }
}

/**
 * Fetch logged-in patient profile data
 * GET {PATIENT_API}/me
 * @param {string} token
 * @returns {Promise<Object|null>} patient object or null
 */
export async function getPatientData(token) {
  try {
    if (!token) return null;

    const res = await fetch(`${PATIENT_API}/me`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    if (!res.ok) {
      console.error("getPatientData failed:", res.status);
      return null;
    }

    const data = await res.json();
    return data ?? null;
  } catch (err) {
    console.error("getPatientData error:", err);
    return null;
  }
}

/**
 * Fetch patient appointments (usable by patient OR doctor dashboard)
 * Dynamic URL pattern based on user role.
 *
 * Suggested patterns:
 * - If user === "patient": GET /patient/{id}/appointments
 * - If user === "doctor":  GET /patient/appointments?patientId={id}
 *
 * NOTE: Adjust the URL logic below if your backend endpoints differ.
 *
 * @param {number|string} id
 * @param {string} token
 * @param {"patient"|"doctor"} user
 * @returns {Promise<Array|null>} appointments array or null if failure
 */
export async function getPatientAppointments(id, token, user) {
  try {
    if (!id || !token) return null;

    let url = "";

    if (user === "doctor") {
      // doctor dashboard fetching patient appointments (example)
      url = `${PATIENT_API}/appointments?patientId=${encodeURIComponent(id)}`;
    } else {
      // patient dashboard fetching own appointments (example)
      url = `${PATIENT_API}/${encodeURIComponent(id)}/appointments`;
    }

    const res = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    if (!res.ok) {
      console.error("getPatientAppointments failed:", res.status);
      return null;
    }

    const data = await res.json();
    return Array.isArray(data) ? data : [];
  } catch (err) {
    console.error("getPatientAppointments error:", err);
    return null;
  }
}

/**
 * Filter appointments
 * GET {PATIENT_API}/appointments/filter?condition=...&name=...
 *
 * @param {string} condition - e.g. "pending" / "consulted"
 * @param {string} name - optional patient name search
 * @param {string} token
 * @returns {Promise<Array>} filtered appointments or []
 */
export async function filterAppointments(condition, name, token) {
  try {
    if (!token) return [];

    const params = new URLSearchParams();
    if (condition && condition.trim()) params.append("condition", condition.trim());
    if (name && name.trim()) params.append("name", name.trim());

    const url = `${PATIENT_API}/appointments/filter?${params.toString()}`;

    const res = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    if (!res.ok) {
      console.error("filterAppointments failed:", res.status);
      return [];
    }

    const data = await res.json();
    return Array.isArray(data) ? data : [];
  } catch (err) {
    console.error("filterAppointments error:", err);
    alert("Unexpected error while filtering appointments.");
    return [];
  }
}
