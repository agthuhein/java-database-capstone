package com.project.back_end.services;
import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    // 1) savePrescription(Prescription prescription)
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> res = new HashMap<>();

        try {
            prescriptionRepository.save(prescription);
            res.put("message", "Prescription saved");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            res.put("message", "Something went wrong while saving prescription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 2) getPrescription(Long appointmentId)
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> res = new HashMap<>();

        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            res.put("prescriptions", prescriptions);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("message", "Something went wrong while fetching prescription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

}
