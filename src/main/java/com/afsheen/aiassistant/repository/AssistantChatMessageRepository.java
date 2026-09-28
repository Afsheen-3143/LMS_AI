package com.afsheen.aiassistant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.afsheen.aiassistant.entity.AssistantChatMessage;

@Repository
public interface AssistantChatMessageRepository extends JpaRepository<AssistantChatMessage, Long> {

    List<AssistantChatMessage> findByConversationIdOrderBySequenceNoAsc(String conversationId);

    List<String> findDistinctConversationIdByStudentId(String studentId);

    void deleteByConversationId(String conversationId);

    long countByConversationId(String conversationId);
}
