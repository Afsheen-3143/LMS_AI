package com.afsheen.aiassistant.lms.service;

import java.util.List;

import com.afsheen.aiassistant.lms.dto.response.EnrollmentBatchResponse;

public interface EnrollmentBatchService {

    List<EnrollmentBatchResponse> getEnrolledBatchesByStudentId(String studentId);
}
