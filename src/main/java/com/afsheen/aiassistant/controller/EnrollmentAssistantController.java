package com.afsheen.aiassistant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.afsheen.aiassistant.dto.request.AssistantChatRequest;
import com.afsheen.aiassistant.dto.request.EnrollmentConfirmRequest;
import com.afsheen.aiassistant.dto.response.AssistantChatResponse;
import com.afsheen.aiassistant.dto.response.ChatMessageResponse;
import com.afsheen.aiassistant.dto.response.EnrollmentConfirmResponse;
import com.afsheen.aiassistant.service.EnrollmentAssistantService;
import com.afsheen.aiassistant.config.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/student-assistant")
public class EnrollmentAssistantController {

    private final EnrollmentAssistantService assistantService;
    private final JwtUtil jwtUtil;

    public EnrollmentAssistantController(
            EnrollmentAssistantService assistantService,
            JwtUtil jwtUtil) {
        this.assistantService = assistantService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/chat")
    public ResponseEntity<AssistantChatResponse> chat(
            @RequestBody AssistantChatRequest request) {

        String studentId = extractStudentIdFromJwt();

        AssistantChatResponse response = assistantService.chat(request, studentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(@PathVariable String sessionId) {
        String studentId = extractStudentIdFromJwt();

        List<ChatMessageResponse> history = assistantService.getHistory(sessionId, studentId);

        return ResponseEntity.ok(history);
    }

    @PostMapping("/confirm-enrollment")
    public ResponseEntity<EnrollmentConfirmResponse> confirmEnrollment(
            @RequestBody EnrollmentConfirmRequest request) {

        String studentId = extractStudentIdFromJwt();

        EnrollmentConfirmResponse response = assistantService.confirmEnrollment(request, studentId);

        return ResponseEntity.ok(response);
    }

    private String extractStudentIdFromJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpRequest = attrs.getRequest();

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }

        throw new RuntimeException("Not authenticated");
    }
}
