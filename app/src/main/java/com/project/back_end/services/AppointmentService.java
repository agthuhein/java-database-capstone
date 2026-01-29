package com.project.back_end.services;
import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }
    public int bookAppointment(Appointment appointment) {
        try {
            String validationError = validateAppointment(appointment, false);
            if (validationError != null) {
                return 0;
            }
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> res = new HashMap<>();

        if (appointment == null || appointment.getId() == null) {
            res.put("message", "Appointment ID is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
        if (existingOpt.isEmpty()) {
            res.put("message", "Appointment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        String validationError = validateAppointment(appointment, true);
        if (validationError != null) {
            res.put("message", validationError);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        try {
            appointmentRepository.save(appointment);
            res.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("message", "Failed to update appointment");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> res = new HashMap<>();

        if (token == null || token.isBlank()) {
            res.put("message", "Missing token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            res.put("message", "Appointment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        Appointment appointment = opt.get();

        // Ensure only the patient who booked can cancel
        try {
            String patientEmailFromToken = tokenService.extractEmail(token);
            if (appointment.getPatient() == null || appointment.getPatient().getEmail() == null) {
                res.put("message", "Appointment has no patient information");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
            }

            if (!appointment.getPatient().getEmail().equalsIgnoreCase(patientEmailFromToken)) {
                res.put("message", "Unauthorized: you can only cancel your own appointment");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
            }
        } catch (Exception e) {
            res.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        try {
            appointmentRepository.delete(appointment);
            res.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("message", "Failed to cancel appointment");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> result = new HashMap<>();

        if (token == null || token.isBlank()) {
            result.put("appointments", List.of());
            result.put("message", "Missing token");
            return result;
        }

        if (date == null) {
            result.put("appointments", List.of());
            result.put("message", "Date is required");
            return result;
        }

        Doctor doctor;
        try {
            String doctorEmail = tokenService.extractEmail(token);
            doctor = doctorRepository.findByEmail(doctorEmail);
            if (doctor == null) {
                result.put("appointments", List.of());
                result.put("message", "Doctor not found");
                return result;
            }
        } catch (Exception e) {
            result.put("appointments", List.of());
            result.put("message", "Invalid token");
            return result;
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> appointments;
        boolean hasPatientFilter = pname != null && !pname.isBlank() && !"null".equalsIgnoreCase(pname);

        if (hasPatientFilter) {
            appointments = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctor.getId(), pname, start, end
                    );
        } else {
            appointments = appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);
        }

        List<AppointmentDTO> dtoList = appointments.stream()
                .map(this::toDTO)
                .toList();

        result.put("appointments", dtoList);
        return result;
    }

    // ---------- Helpers ----------

    private String validateAppointment(Appointment appointment, boolean isUpdate) {
        if (appointment == null) return "Appointment data is required";

        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return "Doctor is required";
        }
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            return "Patient is required";
        }
        if (appointment.getAppointmentTime() == null) {
            return "Appointment time is required";
        }
        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
            return "Appointment time must be in the future";
        }

        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
        if (doctor == null) return "Invalid doctor ID";

        Patient patient = patientRepository.findById(appointment.getPatient().getId()).orElse(null);
        if (patient == null) return "Invalid patient ID";

        // Optional conflict check: avoid overlapping bookings for same doctor within 1 hour
        LocalDateTime apptStart = appointment.getAppointmentTime();
        LocalDateTime apptEnd = apptStart.plusHours(1);

        LocalDateTime dayStart = apptStart.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = apptStart.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> sameDay = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), dayStart, dayEnd);

        for (Appointment a : sameDay) {
            if (isUpdate && appointment.getId() != null && a.getId().equals(appointment.getId())) {
                continue;
            }

            LocalDateTime existingStart = a.getAppointmentTime();
            LocalDateTime existingEnd = existingStart.plusHours(1);

            boolean overlap = apptStart.isBefore(existingEnd) && existingStart.isBefore(apptEnd);
            if (overlap) {
                return "Appointment slot already booked";
            }
        }

        return null;
    }

    private AppointmentDTO toDTO(Appointment a) {
        Doctor d = a.getDoctor();
        Patient p = a.getPatient();

        return new AppointmentDTO(
                a.getId(),
                d != null ? d.getId() : null,
                d != null ? d.getName() : null,
                p != null ? p.getId() : null,
                p != null ? p.getName() : null,
                p != null ? p.getEmail() : null,
                p != null ? p.getPhone() : null,
                p != null ? p.getAddress() : null,
                a.getAppointmentTime(),
                a.getStatus()
        );
    }
}
