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
import com.afsheen.aiassistant.lms.dto.request.ClassScheduleRequest;
import com.afsheen.aiassistant.lms.dto.response.ClassScheduleResponse;
import com.afsheen.aiassistant.lms.entity.ClassBatch;
import com.afsheen.aiassistant.lms.entity.ClassSchedule;
import com.afsheen.aiassistant.lms.repository.ClassBatchRepository;
import com.afsheen.aiassistant.lms.repository.ClassScheduleRepository;

import jakarta.validation.Valid;

/**
 * Admin CRUD over the weekly class schedule. Goes straight to the repositories, same convention as
 * {@link ClassBatchController}.
 */
@RestController
@RequestMapping("/api/admin/lms")
public class ClassScheduleController {

    private final ClassScheduleRepository classScheduleRepository;
    private final ClassBatchRepository classBatchRepository;

    public ClassScheduleController(
            ClassScheduleRepository classScheduleRepository, ClassBatchRepository classBatchRepository) {
        this.classScheduleRepository = classScheduleRepository;
        this.classBatchRepository = classBatchRepository;
    }

    @GetMapping("/schedules")
    public ResponseEntity<List<ClassScheduleResponse>> getAllSchedules() {
        return ResponseEntity.ok(classScheduleRepository.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/batches/{batchId}/schedules")
    public ResponseEntity<List<ClassScheduleResponse>> getSchedulesForBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(classScheduleRepository.findByClassBatch_Id(batchId).stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/schedules/{id}")
    public ResponseEntity<ClassScheduleResponse> getSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(findSchedule(id)));
    }

    @PostMapping("/schedules")
    public ResponseEntity<ClassScheduleResponse> createSchedule(@Valid @RequestBody ClassScheduleRequest request) {
        ClassSchedule schedule = new ClassSchedule();
        schedule.setClassBatch(findBatch(request.getClassBatchId()));
        applyRequest(schedule, request);
        return ResponseEntity.ok(toResponse(classScheduleRepository.save(schedule)));
    }

    @PutMapping("/schedules/{id}")
    public ResponseEntity<ClassScheduleResponse> updateSchedule(
            @PathVariable Long id, @Valid @RequestBody ClassScheduleRequest request) {
        ClassSchedule schedule = findSchedule(id);
        schedule.setClassBatch(findBatch(request.getClassBatchId()));
        applyRequest(schedule, request);
        return ResponseEntity.ok(toResponse(classScheduleRepository.save(schedule)));
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        classScheduleRepository.delete(findSchedule(id));
        return ResponseEntity.noContent().build();
    }

    private ClassSchedule findSchedule(Long id) {
        return classScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with ID: " + id));
    }

    private ClassBatch findBatch(Long batchId) {
        return classBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + batchId));
    }

    private static void applyRequest(ClassSchedule schedule, ClassScheduleRequest request) {
        schedule.setClassName(request.getClassName());
        schedule.setClassDate(request.getClassDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setMode(request.getMode());
        schedule.setStatus(request.getStatus());
    }

    private ClassScheduleResponse toResponse(ClassSchedule s) {
        ClassScheduleResponse r = new ClassScheduleResponse();
        r.setId(s.getId());
        r.setClassBatchId(s.getClassBatchId());
        r.setClassName(s.getClassName());
        r.setClassDate(s.getClassDate());
        r.setStartTime(s.getStartTime());
        r.setEndTime(s.getEndTime());
        r.setMode(s.getMode());
        r.setStatus(s.getStatus());
        return r;
    }
}
