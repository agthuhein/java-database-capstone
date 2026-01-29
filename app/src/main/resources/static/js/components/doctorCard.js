import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";


export function createDoctorCard(doctor) {
    // Main card container
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    // Fetch the user's role
    const role = localStorage.getItem("userRole");

    // Doctor Info section
    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor?.name ?? "Unknown Doctor";

    const specialization = document.createElement("p");
    specialization.textContent = `Specialty: ${doctor?.specialty ?? "N/A"}`;

    const email = document.createElement("p");
    email.textContent = `Email: ${doctor?.email ?? "N/A"}`;

    const phone = document.createElement("p");
    phone.textContent = `Phone: ${doctor?.phone ?? "N/A"}`;

    const availability = document.createElement("p");
    const times = Array.isArray(doctor?.availableTimes) ? doctor.availableTimes.join(", ") : "N/A";
    availability.textContent = `Availability: ${times}`;

    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(phone);
    infoDiv.appendChild(availability);

    // Actions container
    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    // Conditionally add buttons based on role
    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";
        removeBtn.classList.add("button", "dangerBtn");

        removeBtn.addEventListener("click", async () => {
            try {
                // 1) Confirm deletion
                const ok = confirm(`Delete Dr. ${doctor?.name ?? ""}?`);
                if (!ok) return;

                // 2) Get token
                const token = localStorage.getItem("token");
                if (!token) {
                    alert("Missing token. Please login again.");
                    return;
                }

                // 3) Call API to delete
                await deleteDoctor(doctor.id, token);

                // 4) On success: remove from DOM
                card.remove();
            } catch (err) {
                console.error("Delete doctor failed:", err);
                alert("Failed to delete doctor. Please try again.");
            }
        });

        actionsDiv.appendChild(removeBtn);
    } else if (role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("button");

        bookNow.addEventListener("click", () => {
            alert("Patient needs to login first.");
        });

        actionsDiv.appendChild(bookNow);
    } else if (role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("button");

        bookNow.addEventListener("click", async (e) => {
            try {
                const token = localStorage.getItem("token");
                if (!token) {
                    alert("Session expired. Please login again.");
                    localStorage.setItem("userRole", "patient");
                    window.location.href = "/pages/patientDashboard.html";
                    return;
                }

                // Fetch patient data before booking
                const patientData = await getPatientData(token);

                // Call booking overlay function (implemented in modals/services)
                // We try a few common placements to avoid breaking.
                if (typeof window.showBookingOverlay === "function") {
                    window.showBookingOverlay(e, doctor, patientData);
                } else if (typeof window.openModal === "function") {
                    // fallback: open booking modal if your modal system supports this type
                    window.openModal("bookAppointment", { doctor, patientData, event: e });
                } else {
                    // fallback event-based approach
                    window.dispatchEvent(
                        new CustomEvent("booking:open", {
                            detail: { doctor, patientData, event: e },
                        })
                    );
                }
            } catch (err) {
                console.error("Booking init failed:", err);
                alert("Unable to start booking. Please try again.");
            }
        });

        actionsDiv.appendChild(bookNow);
    }

    // Final assembly
    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card;
}
