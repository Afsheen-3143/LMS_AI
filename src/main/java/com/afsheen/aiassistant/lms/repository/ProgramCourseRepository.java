package com.afsheen.aiassistant.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.afsheen.aiassistant.lms.entity.ProgramCourse;

@Repository
public interface ProgramCourseRepository extends JpaRepository<ProgramCourse, Long> {

    List<ProgramCourse> findByProgram_ProgramIdOrderBySortOrderAsc(String programId);
}
