// app/src/main/resources/static/js/services/doctorServices.js

import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + "/doctor";

/**
 * Get all doctors
 * GET {DOCTOR_API}
 * @returns {Promise<Array>} list of doctors ([]) if error
 */
export async function getDoctors() {
    try {
        const res = await fetch(DOCTOR_API, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
        });

        if (!res.ok) {
            console.error("getDoctors failed:", res.status);
            return [];
        }

        const data = await res.json();
        // Expected: data is an array of doctors
        return Array.isArray(data) ? data : [];
    } catch (err) {
        console.error("getDoctors error:", err);
        return [];
    }
}

/**
 * Delete a doctor (Admin only)
 * DELETE {DOCTOR_API}/{id}
 * @param {number|string} id
 * @param {string} token
 * @returns {Promise<{success: boolean, message: string}>}
 */
export async function deleteDoctor(id, token) {
    try {
        if (!id) return { success: false, message: "Doctor id is required." };
        if (!token) return { success: false, message: "Token is required." };

        const res = await fetch(`${DOCTOR_API}/${id}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
        });

        let data = null;
        try {
            data = await res.json();
        } catch (_) {
            // if backend returns no JSON
        }

        if (!res.ok) {
            const msg = data?.message || "Failed to delete doctor.";
            return { success: false, message: msg };
        }

        return { success: true, message: data?.message || "Doctor deleted successfully." };
    } catch (err) {
        console.error("deleteDoctor error:", err);
        return { success: false, message: "Something went wrong while deleting doctor." };
    }
}

/**
 * Save (Add) a new doctor (Admin only)
 * POST {DOCTOR_API}
 * @param {Object} doctor
 * @param {string} token
 * @returns {Promise<{success: boolean, message: string, data?: any}>}
 */
export async function saveDoctor(doctor, token) {
    try {
        if (!doctor) return { success: false, message: "Doctor data is required." };
        if (!token) return { success: false, message: "Token is required." };

        const res = await fetch(DOCTOR_API, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(doctor),
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            return {
                success: false,
                message: data?.message || "Failed to add doctor.",
            };
        }

        return {
            success: true,
            message: data?.message || "Doctor added successfully.",
            data,
        };
    } catch (err) {
        console.error("saveDoctor error:", err);
        return { success: false, message: "Something went wrong while saving doctor." };
    }
}

/**
 * Filter doctors by name/time/specialty.
 * This builds a GET URL using query parameters (safe + flexible).
 *
 * Example:
 * GET {DOCTOR_API}/filter?name=Ali&time=AM&specialty=Cardiology
 *
 * @param {string} name
 * @param {string} time
 * @param {string} specialty
 * @returns {Promise<Array>} filtered doctors or []
 */
export async function filterDoctors(name, time, specialty) {
    try {
        const params = new URLSearchParams();

        if (name && name.trim()) params.append("name", name.trim());
        if (time && time.trim()) params.append("time", time.trim());
        if (specialty && specialty.trim()) params.append("specialty", specialty.trim());

        // If no filters, just return all doctors
        if ([...params.keys()].length === 0) {
            return await getDoctors();
        }

        const url = `${DOCTOR_API}/filter?${params.toString()}`;

        const res = await fetch(url, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
        });

        if (!res.ok) {
            console.error("filterDoctors failed:", res.status);
            alert("Failed to filter doctors. Please try again.");
            return [];
        }

        const data = await res.json();
        return Array.isArray(data) ? data : [];
    } catch (err) {
        console.error("filterDoctors error:", err);
        alert("Unexpected error while filtering doctors.");
        return [];
    }
}
