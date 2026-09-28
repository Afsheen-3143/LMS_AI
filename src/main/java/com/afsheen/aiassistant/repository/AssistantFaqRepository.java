package com.afsheen.aiassistant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.afsheen.aiassistant.entity.AssistantFaq;

@Repository
public interface AssistantFaqRepository extends JpaRepository<AssistantFaq, Long> {

    List<AssistantFaq> findByActiveTrue();
}
