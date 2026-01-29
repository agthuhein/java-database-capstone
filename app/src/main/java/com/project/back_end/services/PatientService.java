package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 1) createPatient(Patient patient)
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 2) getPatientAppointment(Long id, String token)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> res = new HashMap<>();

        if (id == null || token == null || token.isBlank()) {
            res.put("message", "Missing patientId or token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        Patient patientFromToken = getPatientFromToken(token);
        if (patientFromToken == null) {
            res.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        if (!patientFromToken.getId().equals(id)) {
            res.put("message", "Unauthorized: patientId does not match token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        List<Appointment> appointments = appointmentRepository.findByPatientId(id);
        List<AppointmentDTO> dtoList = appointments.stream().map(this::toDTO).toList();

        res.put("appointments", dtoList);
        return ResponseEntity.ok(res);
    }

    // 3) filterByCondition(String condition, Long id)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> res = new HashMap<>();

        if (id == null) {
            res.put("message", "Missing patientId");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        int status = conditionToStatus(condition);
        if (status == Integer.MIN_VALUE) {
            res.put("message", "Invalid condition. Use 'past' or 'future'");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        List<Appointment> appointments = appointmentRepository
                .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);

        List<AppointmentDTO> dtoList = appointments.stream().map(this::toDTO).toList();
        res.put("appointments", dtoList);
        return ResponseEntity.ok(res);
    }

    // 4) filterByDoctor(String name, Long patientId)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> res = new HashMap<>();

        if (patientId == null) {
            res.put("message", "Missing patientId");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        String doctorName = (name == null) ? "" : name.trim();
        List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);

        List<AppointmentDTO> dtoList = appointments.stream().map(this::toDTO).toList();
        res.put("appointments", dtoList);
        return ResponseEntity.ok(res);
    }

    // 5) filterByDoctorAndCondition(String condition, String name, long patientId)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> res = new HashMap<>();

        int status = conditionToStatus(condition);
        if (status == Integer.MIN_VALUE) {
            res.put("message", "Invalid condition. Use 'past' or 'future'");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        String doctorName = (name == null) ? "" : name.trim();

        List<Appointment> appointments = appointmentRepository
                .filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);

        List<AppointmentDTO> dtoList = appointments.stream().map(this::toDTO).toList();
        res.put("appointments", dtoList);
        return ResponseEntity.ok(res);
    }

    // 6) getPatientDetails(String token)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> res = new HashMap<>();

        if (token == null || token.isBlank()) {
            res.put("message", "Missing token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        Patient patient = getPatientFromToken(token);
        if (patient == null) {
            res.put("message", "Invalid token or patient not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        res.put("patient", patient);
        return ResponseEntity.ok(res);
    }

    // ---------- Helpers ----------

    private Patient getPatientFromToken(String token) {
        try {
            String email = tokenService.extractEmail(token);
            if (email == null || email.isBlank()) return null;
            return patientRepository.findByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    private int conditionToStatus(String condition) {
        if (condition == null) return Integer.MIN_VALUE;

        String c = condition.trim().toLowerCase();
        if (c.equals("past")) return 1;
        if (c.equals("future")) return 0;

        return Integer.MIN_VALUE;
    }

    private AppointmentDTO toDTO(Appointment a) {
        var d = a.getDoctor();
        var p = a.getPatient();

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
