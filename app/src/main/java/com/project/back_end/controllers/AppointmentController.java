package com.project.back_end.controllers;


import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.ClinicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final ClinicService clinicService;

    public AppointmentController(AppointmentService appointmentService, ClinicService clinicService) {
        this.appointmentService = appointmentService;
        this.clinicService = clinicService;
    }

    // Doctor: get appointments by date + patientName (optional)
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token
    ) {
        // validate doctor token
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "doctor");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.putAll(validation.getBody());
            return ResponseEntity.status(validation.getStatusCode()).body(error);
        }

        // normalize patientName to allow "null" string from frontend
        String pname = ("null".equalsIgnoreCase(patientName) || patientName.isBlank()) ? null : patientName;

        Map<String, Object> response = appointmentService.getAppointment(pname, date, token);
        return ResponseEntity.ok(response);
    }

    // Patient: book appointment
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment
    ) {
        // validate patient token
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "patient");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            return ResponseEntity.status(validation.getStatusCode()).body(validation.getBody());
        }

        // validate appointment slot / doctor existence
        int valid = clinicService.validateAppointment(appointment);
        if (valid == -1) {
            Map<String, String> res = new HashMap<>();
            res.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        if (valid == 0) {
            Map<String, String> res = new HashMap<>();
            res.put("message", "Selected time is not available");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
        }

        int booked = appointmentService.bookAppointment(appointment);
        if (booked == 1) {
            Map<String, String> res = new HashMap<>();
            res.put("message", "Appointment booked successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }

        Map<String, String> res = new HashMap<>();
        res.put("message", "Failed to book appointment");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    // Patient: update appointment
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment
    ) {
        // validate patient token
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "patient");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            return ResponseEntity.status(validation.getStatusCode()).body(validation.getBody());
        }

        // AppointmentService handles existence + validateAppointment internally (as per your spec)
        return appointmentService.updateAppointment(appointment);
    }

    // Patient: cancel appointment
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token
    ) {
        // validate patient token
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "patient");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            return ResponseEntity.status(validation.getStatusCode()).body(validation.getBody());
        }

        return appointmentService.cancelAppointment(id, token);
    }
}
