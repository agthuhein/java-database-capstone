package com.project.back_end.services;


import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.*;

@Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // ------------------ 1) validateToken ------------------
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> res = new HashMap<>();

        if (token == null || token.isBlank()) {
            res.put("message", "Missing token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        boolean valid = tokenService.validateToken(token, user);
        if (!valid) {
            res.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        // requirement says: return an error message if invalid/expired
        // so for valid token, return empty map (helps dashboard controller check isEmpty()).
        return ResponseEntity.ok(res);
    }

    // ------------------ 2) validateAdmin ------------------
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> res = new HashMap<>();

        if (receivedAdmin == null
                || receivedAdmin.getUsername() == null
                || receivedAdmin.getPassword() == null) {
            res.put("message", "Missing credentials");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
        if (admin == null) {
            res.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
            res.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        // Token subject = username for admin (as your LoginDTO design notes)
        String token = tokenService.generateToken(admin.getUsername());
        res.put("token", token);
        res.put("message", "Login successful");

        return ResponseEntity.ok(res);
    }

    // ------------------ 3) filterDoctor ------------------
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        // This method returns Map<String,Object> with "doctors"
        // and coordinates calls to DoctorService based on which filters are present.

        boolean hasName = !isBlank(name);
        boolean hasSpec = !isBlank(specialty);
        boolean hasTime = !isBlank(time);

        if (hasName && hasSpec && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        }

        if (hasName && hasTime && !hasSpec) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        }

        if (hasName && hasSpec && !hasTime) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        }

        if (hasSpec && hasTime && !hasName) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        }

        if (hasSpec && !hasName && !hasTime) {
            return doctorService.filterDoctorBySpecility(specialty);
        }

        if (hasTime && !hasName && !hasSpec) {
            return doctorService.filterDoctorsByTime(time);
        }

        if (hasName && !hasSpec && !hasTime) {
            return doctorService.findDoctorByName(name);
        }

        // If no filters at all, return all doctors
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", doctorService.getDoctors());
        return res;
    }

    // ------------------ 4) validateAppointment ------------------
    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return -1;
        }

        Long doctorId = appointment.getDoctor().getId();

        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return -1;

        if (appointment.getAppointmentTime() == null) return 0;

        LocalDate date = appointment.getAppointmentTime().toLocalDate();
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);

        // Match appointment time against available slots
        // Slot format assumed like "09:00 AM" or "09:00"
        String apptTime = appointment.getAppointmentTime().toLocalTime().toString(); // e.g. "09:00"
        boolean ok = availableSlots.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .anyMatch(slot -> normalizeSlot(slot).equals(apptTime));

        return ok ? 1 : 0;
    }

    // ------------------ 5) validatePatient (register validation) ------------------
    public boolean validatePatient(Patient patient) {
        if (patient == null) return false;

        String email = patient.getEmail();
        String phone = patient.getPhone();

        // true if patient does NOT exist, false if exists already (as requirement says)
        Patient existing = patientRepository.findByEmailOrPhone(email, phone);
        return existing == null;
    }

    // ------------------ 6) validatePatientLogin ------------------
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> res = new HashMap<>();

        if (login == null || isBlank(login.getIdentifier()) || isBlank(login.getPassword())) {
            res.put("message", "Missing credentials");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        Patient patient = patientRepository.findByEmail(login.getIdentifier());
        if (patient == null) {
            res.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        if (!patient.getPassword().equals(login.getPassword())) {
            res.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        String token = tokenService.generateToken(patient.getEmail());
        res.put("token", token);
        res.put("message", "Login successful");

        return ResponseEntity.ok(res);
    }

    // ------------------ 7) filterPatient ------------------
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Map<String, Object> res = new HashMap<>();

        if (token == null || token.isBlank()) {
            res.put("message", "Missing token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        Patient patient;
        try {
            String email = tokenService.extractEmail(token);
            patient = patientRepository.findByEmail(email);
            if (patient == null) {
                res.put("message", "Invalid token or patient not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }
        } catch (Exception e) {
            res.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        boolean hasCondition = !isBlank(condition);
        boolean hasDoctorName = !isBlank(name);

        if (hasCondition && hasDoctorName) {
            return patientService.filterByDoctorAndCondition(condition, name, patient.getId());
        }

        if (hasCondition) {
            return patientService.filterByCondition(condition, patient.getId());
        }

        if (hasDoctorName) {
            return patientService.filterByDoctor(name, patient.getId());
        }

        // No filters: return all appointments for the patient
        return patientService.getPatientAppointment(patient.getId(), token);
    }

    // ------------------ Helpers ------------------

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty() || "null".equalsIgnoreCase(s.trim());
    }

    // Normalizes slot to "HH:mm" (so it can match LocalTime.toString() which is usually "HH:mm" or "HH:mm:ss")
    private String normalizeSlot(String slot) {
        if (slot == null) return "";

        String s = slot.trim().toUpperCase();

        // If stored like "09:00 AM" -> take first part "09:00"
        if (s.endsWith("AM") || s.endsWith("PM")) {
            String timePart = s.replace("AM", "").replace("PM", "").trim();
            // If you store "9:00 AM", LocalTime.toString() becomes "09:00"
            // We'll try to pad if needed:
            return padTime(timePart);
        }

        // If stored like "09:00" -> return it padded
        return padTime(s);
    }

    private String padTime(String t) {
        // Very small helper: "9:00" -> "09:00"
        // If it's already "09:00" return as is.
        try {
            String[] parts = t.split(":");
            if (parts.length >= 2) {
                String hh = parts[0].trim();
                String mm = parts[1].trim();
                if (hh.length() == 1) hh = "0" + hh;
                if (mm.length() == 1) mm = "0" + mm;
                return hh + ":" + mm;
            }
        } catch (Exception ignored) {}
        return t;
    }
}
