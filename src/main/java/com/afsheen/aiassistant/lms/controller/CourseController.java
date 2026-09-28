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

import com.afsheen.aiassistant.lms.dto.request.CourseRequest;
import com.afsheen.aiassistant.lms.dto.response.CourseResponse;
import com.afsheen.aiassistant.lms.service.CourseManagementService;

import jakarta.validation.Valid;

/** Admin CRUD over the course catalog. Locked to ROLE_ADMIN in SecurityConfig. */
@RestController
@RequestMapping("/api/admin/lms/courses")
public class CourseController {

    private final CourseManagementService courseManagementService;

    public CourseController(CourseManagementService courseManagementService) {
        this.courseManagementService = courseManagementService;
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseManagementService.viewAllCourses());
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(courseManagementService.getCourseById(courseId));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseManagementService.createCourse(request));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable String courseId, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseManagementService.updateCourse(courseId, request));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String courseId) {
        courseManagementService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}
