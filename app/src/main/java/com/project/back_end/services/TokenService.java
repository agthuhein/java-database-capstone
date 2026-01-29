package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // Generate JWT for username/email (Admin uses username, Doctor/Patient uses email)
    public String generateToken(String identifier) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (7L * 24 * 60 * 60 * 1000)); // 7 days

        return Jwts.builder()
                .subject(identifier)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Extract subject (identifier) from token
    public String extractIdentifier(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    // Optional compatibility helper if other services call extractEmail()
    public String extractEmail(String token) {
        return extractIdentifier(token);
    }

    // Validate token for a role (admin/doctor/patient)
    public boolean validateToken(String token, String user) {
        try {
            String identifier = extractIdentifier(token);
            if (identifier == null || user == null) return false;

            return switch (user.toLowerCase()) {
                case "admin" -> adminRepository.findByUsername(identifier) != null;
                case "doctor" -> doctorRepository.findByEmail(identifier) != null;
                case "patient" -> patientRepository.findByEmail(identifier) != null;
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        // HS256 needs a sufficiently long secret (>= 32 chars recommended)
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
