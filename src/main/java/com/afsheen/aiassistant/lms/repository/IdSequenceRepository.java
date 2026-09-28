package com.afsheen.aiassistant.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.afsheen.aiassistant.lms.entity.IdSequence;

@Repository
public interface IdSequenceRepository extends JpaRepository<IdSequence, String> {
}
