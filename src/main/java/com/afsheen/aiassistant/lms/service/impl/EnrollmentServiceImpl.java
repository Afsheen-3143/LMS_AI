package com.afsheen.aiassistant.lms.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.lms.dto.request.EnrollmentRequest;
import com.afsheen.aiassistant.lms.dto.response.EnrollmentResponse;
import com.afsheen.aiassistant.lms.entity.Enrollment;
import com.afsheen.aiassistant.lms.entity.EnrollmentStatus;
import com.afsheen.aiassistant.lms.repository.CourseRepository;
import com.afsheen.aiassistant.lms.repository.EnrollmentRepository;
import com.afsheen.aiassistant.lms.service.EnrollmentService;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(request.getStudentId());
        enrollment.setCourseId(request.getCourseId());
        enrollment.setProgramId(request.getProgramId());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setPaymentStatus("PENDING");

        if (request.getCourseId() != null) {
            courseRepository.findById(request.getCourseId())
                    .ifPresent(c -> enrollment.setCourseTitle(c.getCourseTitle()));
        }

        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public List<EnrollmentResponse> getEnrollmentsByStudent(String studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(EnrollmentServiceImpl::toResponse)
                .toList();
    }

    private static EnrollmentResponse toResponse(Enrollment e) {
        EnrollmentResponse r = new EnrollmentResponse();
        r.setId(e.getId());
        r.setStudentId(e.getStudentId());
        r.setCourseId(e.getCourseId());
        r.setCourseTitle(e.getCourseTitle());
        r.setProgramId(e.getProgramId());
        r.setStatus(e.getStatus());
        r.setPaymentStatus(e.getPaymentStatus());
        return r;
    }
}
