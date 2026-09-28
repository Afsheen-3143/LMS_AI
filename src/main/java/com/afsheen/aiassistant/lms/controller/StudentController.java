package com.afsheen.aiassistant.lms.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.afsheen.aiassistant.lms.dto.request.StudentRegistrationRequest;
import com.afsheen.aiassistant.lms.dto.response.StudentResponse;
import com.afsheen.aiassistant.lms.service.StudentService;

import jakarta.validation.Valid;

/** Admin CRUD over students. Locked to ROLE_ADMIN in SecurityConfig. */
@RestController
@RequestMapping("/api/admin/lms/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(studentService.getStudentById(studentId));
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRegistrationRequest request) {
        return ResponseEntity.ok(studentService.registerStudent(request));
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable String studentId, @Valid @RequestBody StudentRegistrationRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(studentId, request));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }
}
