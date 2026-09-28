package com.afsheen.aiassistant.lms.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.lms.dto.response.EnrollmentBatchResponse;
import com.afsheen.aiassistant.lms.entity.EnrollmentBatch;
import com.afsheen.aiassistant.lms.repository.EnrollmentBatchRepository;
import com.afsheen.aiassistant.lms.service.EnrollmentBatchService;

@Service
public class EnrollmentBatchServiceImpl implements EnrollmentBatchService {

    private final EnrollmentBatchRepository enrollmentBatchRepository;

    public EnrollmentBatchServiceImpl(EnrollmentBatchRepository enrollmentBatchRepository) {
        this.enrollmentBatchRepository = enrollmentBatchRepository;
    }

    @Override
    public List<EnrollmentBatchResponse> getEnrolledBatchesByStudentId(String studentId) {
        return enrollmentBatchRepository.findByStudentId(studentId).stream()
                .map(EnrollmentBatchServiceImpl::toResponse)
                .toList();
    }

    private static EnrollmentBatchResponse toResponse(EnrollmentBatch b) {
        EnrollmentBatchResponse r = new EnrollmentBatchResponse();
        r.setEnrollmentId(b.getEnrollmentId());
        r.setStudentId(b.getStudentId());
        r.setCourseId(b.getCourseId());
        r.setBatchId(b.getBatchId());
        r.setBatchName(b.getBatchName());
        return r;
    }
}
