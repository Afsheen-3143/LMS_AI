package com.afsheen.aiassistant.service;

import java.util.List;

import com.afsheen.aiassistant.dto.request.AssistantChatRequest;
import com.afsheen.aiassistant.dto.request.EnrollmentConfirmRequest;
import com.afsheen.aiassistant.dto.response.AssistantChatResponse;
import com.afsheen.aiassistant.dto.response.ChatMessageResponse;
import com.afsheen.aiassistant.dto.response.EnrollmentConfirmResponse;

public interface EnrollmentAssistantService {

    AssistantChatResponse chat(AssistantChatRequest request, String authenticatedStudentId);

    EnrollmentConfirmResponse confirmEnrollment(EnrollmentConfirmRequest request, String authenticatedStudentId);

    List<ChatMessageResponse> getHistory(String conversationId, String authenticatedStudentId);
}
