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

import com.afsheen.aiassistant.exceptions.ResourceNotFoundException;
import com.afsheen.aiassistant.lms.dto.request.ClassBatchRequest;
import com.afsheen.aiassistant.lms.dto.response.ClassBatchResponse;
import com.afsheen.aiassistant.lms.entity.ClassBatch;
import com.afsheen.aiassistant.lms.entity.Course;
import com.afsheen.aiassistant.lms.repository.ClassBatchRepository;
import com.afsheen.aiassistant.lms.repository.ClassStudentRepository;
import com.afsheen.aiassistant.lms.repository.CourseRepository;

import jakarta.validation.Valid;

/**
 * Admin CRUD over class batches. Goes straight to the repositories - like {@code AssistantTools}'
 * read-only batch/schedule calls, there's no dedicated service layer for this entity.
 */
@RestController
@RequestMapping("/api/admin/lms")
public class ClassBatchController {

    private final ClassBatchRepository classBatchRepository;
    private final ClassStudentRepository classStudentRepository;
    private final CourseRepository courseRepository;

    public ClassBatchController(
            ClassBatchRepository classBatchRepository,
            ClassStudentRepository classStudentRepository,
            CourseRepository courseRepository) {
        this.classBatchRepository = classBatchRepository;
        this.classStudentRepository = classStudentRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/batches")
    public ResponseEntity<List<ClassBatchResponse>> getAllBatches() {
        return ResponseEntity.ok(classBatchRepository.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/courses/{courseId}/batches")
    public ResponseEntity<List<ClassBatchResponse>> getBatchesForCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(classBatchRepository.findByCourse_CourseId(courseId).stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<ClassBatchResponse> getBatch(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(findBatch(id)));
    }

    @PostMapping("/batches")
    public ResponseEntity<ClassBatchResponse> createBatch(@Valid @RequestBody ClassBatchRequest request) {
        ClassBatch batch = new ClassBatch();
        batch.setCourse(findCourse(request.getCourseId()));
        applyRequest(batch, request);
        return ResponseEntity.ok(toResponse(classBatchRepository.save(batch)));
    }

    @PutMapping("/batches/{id}")
    public ResponseEntity<ClassBatchResponse> updateBatch(
            @PathVariable Long id, @Valid @RequestBody ClassBatchRequest request) {
        ClassBatch batch = findBatch(id);
        batch.setCourse(findCourse(request.getCourseId()));
        applyRequest(batch, request);
        return ResponseEntity.ok(toResponse(classBatchRepository.save(batch)));
    }

    @DeleteMapping("/batches/{id}")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
        classBatchRepository.delete(findBatch(id));
        return ResponseEntity.noContent().build();
    }

    private ClassBatch findBatch(Long id) {
        return classBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + id));
    }

    private Course findCourse(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    }

    private static void applyRequest(ClassBatch batch, ClassBatchRequest request) {
        batch.setClassName(request.getClassName());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setStatus(request.getStatus());
        batch.setCapacity(request.getCapacity());
    }

    private ClassBatchResponse toResponse(ClassBatch b) {
        ClassBatchResponse r = new ClassBatchResponse();
        r.setId(b.getId());
        r.setCourseId(b.getCourseId());
        r.setCourseTitle(b.getCourse() != null ? b.getCourse().getCourseTitle() : null);
        r.setClassName(b.getClassName());
        r.setStartDate(b.getStartDate());
        r.setEndDate(b.getEndDate());
        r.setStatus(b.getStatus());
        r.setCapacity(b.getCapacity());
        r.setSeatsTaken(classStudentRepository.countByClassBatchId(b.getId()));
        return r;
    }
}
