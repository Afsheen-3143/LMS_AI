package com.afsheen.aiassistant.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.afsheen.aiassistant.lms.entity.Program;

@Repository
public interface ProgramRepository extends JpaRepository<Program, String> {
}
