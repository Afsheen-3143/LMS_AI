package com.afsheen.aiassistant.lms.service;

import java.util.List;

import com.afsheen.aiassistant.lms.dto.request.EnrollmentRequest;
import com.afsheen.aiassistant.lms.dto.response.EnrollmentResponse;

public interface EnrollmentService {

    EnrollmentResponse createEnrollment(EnrollmentRequest request);

    List<EnrollmentResponse> getEnrollmentsByStudent(String studentId);
}
