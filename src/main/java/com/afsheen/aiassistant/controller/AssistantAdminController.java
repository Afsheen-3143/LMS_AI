package com.afsheen.aiassistant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.afsheen.aiassistant.dto.request.AssistantFaqRequest;
import com.afsheen.aiassistant.dto.response.AssistantFaqResponse;
import com.afsheen.aiassistant.rag.DocumentIngestionService;
import com.afsheen.aiassistant.service.AssistantFaqService;

import jakarta.validation.Valid;

/**
 * Admin-only management of the assistant's knowledge base: FAQ content, plus
 * the endpoint to (re)run ingestion after content changes (a course
 * description edited, a new FAQ added, a fee updated). Locked to ROLE_ADMIN
 * in SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/assistant")
public class AssistantAdminController {

    private final AssistantFaqService assistantFaqService;
    private final DocumentIngestionService documentIngestionService;

    public AssistantAdminController(
            AssistantFaqService assistantFaqService,
            DocumentIngestionService documentIngestionService) {
        this.assistantFaqService = assistantFaqService;
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/faqs")
    public ResponseEntity<AssistantFaqResponse> createFaq(@Valid @RequestBody AssistantFaqRequest request) {
        return ResponseEntity.ok(assistantFaqService.createFaq(request));
    }

    @PutMapping("/faqs/{id}")
    public ResponseEntity<AssistantFaqResponse> updateFaq(
            @PathVariable Long id,
            @Valid @RequestBody AssistantFaqRequest request) {
        return ResponseEntity.ok(assistantFaqService.updateFaq(id, request));
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        assistantFaqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/faqs")
    public ResponseEntity<List<AssistantFaqResponse>> getAllFaqs() {
        return ResponseEntity.ok(assistantFaqService.getAllFaqs());
    }

    @PostMapping("/reindex")
    public ResponseEntity<DocumentIngestionService.IngestionResult> reindex() {
        return ResponseEntity.ok(documentIngestionService.reindexAll());
    }
}
