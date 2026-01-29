package com.project.back_end.controllers;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.ClinicService;
import com.project.back_end.services.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {
    private final DoctorService doctorService;
    private final ClinicService clinicService;

    public DoctorController(DoctorService doctorService, ClinicService clinicService) {
        this.doctorService = doctorService;
        this.clinicService = clinicService;
    }

    // 1) Get Doctor Availability
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable LocalDate date,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, user);

        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.putAll(validation.getBody());
            return ResponseEntity.status(validation.getStatusCode()).body(error);
        }

        List<String> slots = doctorService.getDoctorAvailability(doctorId, date);

        Map<String, Object> res = new HashMap<>();
        res.put("availability", slots);
        return ResponseEntity.ok(res);
    }

    // 2) Get List of Doctors
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        List<Doctor> doctors = doctorService.getDoctors();
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", doctors);
        return ResponseEntity.ok(res);
    }

    // 3) Add New Doctor (Admin only)
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "admin");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            return ResponseEntity.status(validation.getStatusCode()).body(validation.getBody());
        }

        int result = doctorService.saveDoctor(doctor);

        Map<String, String> res = new HashMap<>();
        if (result == 1) {
            res.put("message", "Doctor added to db");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } else if (result == -1) {
            res.put("message", "Doctor already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
        } else {
            res.put("message", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 4) Doctor Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    // 5) Update Doctor Details (Admin only)
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "admin");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            return ResponseEntity.status(validation.getStatusCode()).body(validation.getBody());
        }

        int result = doctorService.updateDoctor(doctor);

        Map<String, String> res = new HashMap<>();
        if (result == 1) {
            res.put("message", "Doctor updated");
            return ResponseEntity.ok(res);
        } else if (result == -1) {
            res.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        } else {
            res.put("message", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 6) Delete Doctor (Admin only)
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> validation = clinicService.validateToken(token, "admin");
        if (validation.getBody() != null && !validation.getBody().isEmpty()) {
            return ResponseEntity.status(validation.getStatusCode()).body(validation.getBody());
        }

        int result = doctorService.deleteDoctor(id);

        Map<String, String> res = new HashMap<>();
        if (result == 1) {
            res.put("message", "Doctor deleted successfully");
            return ResponseEntity.ok(res);
        } else if (result == -1) {
            res.put("message", "Doctor not found with id");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        } else {
            res.put("message", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 7) Filter Doctors
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality
    ) {
        // Your frontend may pass "null" as string → normalize
        String n = ("null".equalsIgnoreCase(name) || name.isBlank()) ? null : name;
        String t = ("null".equalsIgnoreCase(time) || time.isBlank()) ? null : time;
        String s = ("null".equalsIgnoreCase(speciality) || speciality.isBlank()) ? null : speciality;

        Map<String, Object> result = clinicService.filterDoctor(n, s, t);
        return ResponseEntity.ok(result);
    }
}
