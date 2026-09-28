package com.afsheen.aiassistant.entity;

import java.time.LocalDateTime;

import org.springframework.ai.chat.messages.MessageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Persists one turn of a conversation so Spring AI's {@code ChatMemory} survives
 * app restarts and so chat history can be served back over the REST API.
 * "conversationId" here is what the API calls a "sessionId" - same value.
 */
@Entity
@Table(name = "assistant_chat_message")
public class AssistantChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "content", nullable = false, columnDefinition = "CLOB")
    private String content;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getSequenceNo() { return sequenceNo; }
    public void setSequenceNo(int sequenceNo) { this.sequenceNo = sequenceNo; }

    public LocalDateTime getCreatedDt() { return createdDt; }
    public void setCreatedDt(LocalDateTime createdDt) { this.createdDt = createdDt; }
}
