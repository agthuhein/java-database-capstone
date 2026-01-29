package com.project.back_end.controllers;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.ClinicService;
import com.project.back_end.services.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private final PatientService patientService;
    private final ClinicService clinicService;

    public PatientController(PatientService patientService, ClinicService clinicService) {
        this.patientService = patientService;
        this.clinicService = clinicService;
    }

    // 1) Get Patient Details (token-based)
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable String token) {

        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "patient");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.putAll(validation.getBody());
            return ResponseEntity.status(validation.getStatusCode()).body(error);
        }

        return patientService.getPatientDetails(token);
    }

    // 2) Create a New Patient (Signup)
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {

        // validate patient does not exist (email or phone)
        boolean okToCreate = clinicService.validatePatient(patient);
        if (!okToCreate) {
            Map<String, String> res = new HashMap<>();
            res.put("message", "Patient with email id or phone no already exist");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
        }

        int created = patientService.createPatient(patient);

        Map<String, String> res = new HashMap<>();
        if (created == 1) {
            res.put("message", "Signup successful");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } else {
            res.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 3) Patient Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(@RequestBody Login login) {
        return clinicService.validatePatientLogin(login);
    }

    // 4) Get Patient Appointments
    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointments(
            @PathVariable Long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "patient");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.putAll(validation.getBody());
            return ResponseEntity.status(validation.getStatusCode()).body(error);
        }

        return patientService.getPatientAppointment(id, token);
    }

    // 5) Filter Patient Appointments
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointments(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "patient");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.putAll(validation.getBody());
            return ResponseEntity.status(validation.getStatusCode()).body(error);
        }

        // frontend might pass "null" when no doctor name filter is given
        String doctorName = ("null".equalsIgnoreCase(name) || name.isBlank()) ? null : name;
        String cond = ("null".equalsIgnoreCase(condition) || condition.isBlank()) ? null : condition;

        return clinicService.filterPatient(cond, doctorName, token);
    }
}


