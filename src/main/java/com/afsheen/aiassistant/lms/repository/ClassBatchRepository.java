package com.afsheen.aiassistant.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.afsheen.aiassistant.lms.entity.ClassBatch;

@Repository
public interface ClassBatchRepository extends JpaRepository<ClassBatch, Long> {

    List<ClassBatch> findByCourse_CourseId(String courseId);
}
