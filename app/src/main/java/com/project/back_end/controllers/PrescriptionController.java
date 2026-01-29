package com.project.back_end.controllers;
import com.project.back_end.models.Prescription;
import com.project.back_end.services.ClinicService;
import com.project.back_end.services.PrescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final ClinicService clinicService;

    public PrescriptionController(PrescriptionService prescriptionService, ClinicService clinicService) {
        this.prescriptionService = prescriptionService;
        this.clinicService = clinicService;
    }

    // 1) Save Prescription (Doctor only)
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable String token,
            @RequestBody Prescription prescription
    ) {
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "doctor");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            return ResponseEntity.status(validation.getStatusCode()).body(validation.getBody());
        }

        return prescriptionService.savePrescription(prescription);
    }

    // 2) Get Prescription by Appointment ID (Doctor only)
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "doctor");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.putAll(validation.getBody());
            return ResponseEntity.status(validation.getStatusCode()).body(error);
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}
