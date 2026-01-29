package com.project.back_end.services;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        if (doctorId == null || date == null) return List.of();

        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return List.of();

        List<String> available = doctor.getAvailableTimes() == null
                ? new ArrayList<>()
                : new ArrayList<>(doctor.getAvailableTimes());

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> appts = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        Set<String> bookedSlots = new HashSet<>();
        for (Appointment a : appts) {
            if (a.getAppointmentTime() != null) {
                bookedSlots.add(a.getAppointmentTime().toLocalTime().toString());
            }
        }

        available.removeIf(slot -> {
            if (slot == null) return true;
            String normalized = slot.trim();
            return bookedSlots.contains(normalized);
        });

        return available;
    }

    public int saveDoctor(Doctor doctor) {
        try {
            if (doctor == null || doctor.getEmail() == null) return 0;

            Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
            if (existing != null) return -1;

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor == null || doctor.getId() == null) return 0;

            boolean exists = doctorRepository.existsById(doctor.getId());
            if (!exists) return -1;

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }

            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> res = new HashMap<>();

        if (login == null || login.getIdentifier() == null || login.getPassword() == null) {
            res.put("message", "Missing credentials");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());
        if (doctor == null) {
            res.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        if (!doctor.getPassword().equals(login.getPassword())) {
            res.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        String token = tokenService.generateToken(doctor.getEmail());
        res.put("token", token);
        res.put("message", "Login successful");
        return ResponseEntity.ok(res);
    }

    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> res = new HashMap<>();
        if (name == null) name = "";
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        res.put("doctors", doctors);
        return res;
    }

    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> res = new HashMap<>();

        List<Doctor> doctors;
        if (isBlank(name)) name = "";
        if (isBlank(specialty)) specialty = "";

        doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        doctors = filterDoctorByTime(doctors, amOrPm);

        res.put("doctors", doctors);
        return res;
    }

    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> res = new HashMap<>();

        if (isBlank(name)) name = "";

        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        doctors = filterDoctorByTime(doctors, amOrPm);

        res.put("doctors", doctors);
        return res;
    }

    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        Map<String, Object> res = new HashMap<>();

        if (isBlank(name)) name = "";
        if (isBlank(specilty)) specilty = "";

        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specilty);
        res.put("doctors", doctors);
        return res;
    }

    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        Map<String, Object> res = new HashMap<>();

        if (isBlank(specilty)) specilty = "";

        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        doctors = filterDoctorByTime(doctors, amOrPm);

        res.put("doctors", doctors);
        return res;
    }

    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        Map<String, Object> res = new HashMap<>();

        if (isBlank(specilty)) specilty = "";

        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        res.put("doctors", doctors);
        return res;
    }

    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> res = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findAll();
        doctors = filterDoctorByTime(doctors, amOrPm);

        res.put("doctors", doctors);
        return res;
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (doctors == null) return List.of();
        if (isBlank(amOrPm)) return doctors;

        String target = amOrPm.trim().toUpperCase(); // "AM" or "PM"
        if (!target.equals("AM") && !target.equals("PM")) return doctors;

        List<Doctor> filtered = new ArrayList<>();
        for (Doctor d : doctors) {
            List<String> times = d.getAvailableTimes();
            if (times == null) continue;

            boolean match = times.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .anyMatch(t -> t.toUpperCase().endsWith(target));

            if (match) filtered.add(d);
        }
        return filtered;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty() || "null".equalsIgnoreCase(s.trim());
    }
}
