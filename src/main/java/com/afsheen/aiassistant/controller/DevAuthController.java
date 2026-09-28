package com.afsheen.aiassistant.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.afsheen.aiassistant.config.JwtUtil;
import com.afsheen.aiassistant.exceptions.ResourceNotFoundException;
import com.afsheen.aiassistant.lms.dto.request.StudentRegistrationRequest;
import com.afsheen.aiassistant.lms.dto.response.StudentResponse;
import com.afsheen.aiassistant.lms.repository.StudentRepository;
import com.afsheen.aiassistant.lms.service.StudentService;

import jakarta.validation.Valid;

/**
 * DEV-ONLY: the original host app issues JWTs through a real
 * register/login/OTP flow that isn't part of this showcase. This mints a
 * token for a demo student id with no password, purely so the
 * chat/history/confirm-enrollment and admin FAQ/reindex endpoints are
 * reachable for a demo. Delete this controller before using any of this
 * code in a real app.
 */
@RestController
public class DevAuthController {

    private final JwtUtil jwtUtil;
    private final StudentRepository studentRepository;
    private final StudentService studentService;

    public DevAuthController(JwtUtil jwtUtil, StudentRepository studentRepository, StudentService studentService) {
        this.jwtUtil = jwtUtil;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
    }

    // DEV-ONLY stand-in for a real sign-up flow: creates a new student row
    // so /api/dev/token/{id} has something real to check the id against
    // afterwards.
    @PostMapping("/api/dev/students/register")
    public StudentResponse register(@Valid @RequestBody StudentRegistrationRequest request) {
        return studentService.registerStudent(request);
    }

    @GetMapping("/api/dev/token/{studentId}")
    public Map<String, String> mintDevToken(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "STUDENT") String role) {

        // ADMIN isn't a real record in the demo student store, so only
        // STUDENT logins are checked against it - otherwise this endpoint
        // would mint a token for a student id that doesn't exist anywhere,
        // and every downstream tool call would silently fail to find them.
        if ("STUDENT".equalsIgnoreCase(role) && studentRepository.findByStudentId(studentId).isEmpty()) {
            throw new ResourceNotFoundException(
                    "No demo student with id " + studentId + ". Try S000001 or S000002.");
        }

        String token = jwtUtil.generateToken(studentId + "@example.com", role, studentId);
        return Map.of("token", token, "studentId", studentId, "role", role);
    }
}
