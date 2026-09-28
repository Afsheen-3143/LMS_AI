package com.afsheen.aiassistant.lms.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.afsheen.aiassistant.lms.dto.response.ProgramResponse;
import com.afsheen.aiassistant.lms.service.CourseManagementService;

/** Read-only program listing, used by the catalog page to show which courses a program bundles. */
@RestController
@RequestMapping("/api/admin/lms/programs")
public class ProgramController {

    private final CourseManagementService courseManagementService;

    public ProgramController(CourseManagementService courseManagementService) {
        this.courseManagementService = courseManagementService;
    }

    @GetMapping
    public ResponseEntity<List<ProgramResponse>> getAllPrograms() {
        return ResponseEntity.ok(courseManagementService.getAllPrograms());
    }

    @GetMapping("/{programId}")
    public ResponseEntity<ProgramResponse> getProgram(@PathVariable String programId) {
        return ResponseEntity.ok(courseManagementService.getProgramById(programId));
    }
}
