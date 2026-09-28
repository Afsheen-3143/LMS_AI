package com.afsheen.aiassistant.lms.service.impl;

import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.lms.dto.request.StudentCourseEnrollRequest;
import com.afsheen.aiassistant.lms.dto.response.StudentCourseResponse;
import com.afsheen.aiassistant.lms.service.StudentCourseService;

/**
 * Demo stand-in: the real host app has a whole StudentCourse join-table
 * concept for tracking per-course enrollment separately from the generic
 * Enrollment record. Here EnrollmentServiceImpl.createEnrollment already
 * covers that in-memory, so this just no-ops - kept only so the real
 * EnrollmentAssistantServiceImpl call site compiles unchanged.
 */
@Service
public class StudentCourseServiceImpl implements StudentCourseService {

    @Override
    public StudentCourseResponse enroll(StudentCourseEnrollRequest request) {
        return new StudentCourseResponse();
    }
}
