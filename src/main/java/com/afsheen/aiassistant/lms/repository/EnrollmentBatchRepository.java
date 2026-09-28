package com.afsheen.aiassistant.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.afsheen.aiassistant.lms.entity.EnrollmentBatch;

@Repository
public interface EnrollmentBatchRepository extends JpaRepository<EnrollmentBatch, Long> {

    List<EnrollmentBatch> findByStudentId(String studentId);
}
