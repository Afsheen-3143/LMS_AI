package com.afsheen.aiassistant.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.dto.request.AssistantChatRequest;
import com.afsheen.aiassistant.dto.request.EnrollmentConfirmRequest;
import com.afsheen.aiassistant.dto.response.AssistantChatResponse;
import com.afsheen.aiassistant.dto.response.ChatMessageResponse;
import com.afsheen.aiassistant.dto.response.EnrollmentConfirmResponse;
import com.afsheen.aiassistant.memory.ConversationIdSupport;
import com.afsheen.aiassistant.prompt.AssistantSystemPrompt;
import com.afsheen.aiassistant.rag.RagRetrievalService;
import com.afsheen.aiassistant.service.EnrollmentAssistantService;
import com.afsheen.aiassistant.tools.AssistantTools;
import com.afsheen.aiassistant.lms.dto.request.AssignProgramRequest;
import com.afsheen.aiassistant.lms.dto.request.EnrollmentRequest;
import com.afsheen.aiassistant.lms.dto.request.StudentCourseEnrollRequest;
import com.afsheen.aiassistant.exceptions.UnauthorizedAccessException;
import com.afsheen.aiassistant.repository.AssistantChatMessageRepository;
import com.afsheen.aiassistant.lms.repository.StudentRepository;
import com.afsheen.aiassistant.lms.service.EnrollmentService;
import com.afsheen.aiassistant.lms.service.StudentCourseService;
import com.afsheen.aiassistant.lms.service.StudentProgramService;

@Service
public class EnrollmentAssistantServiceImpl implements EnrollmentAssistantService {

    private static final Logger logger = LogManager.getLogger(EnrollmentAssistantServiceImpl.class);

    // How many knowledge-base chunks to retrieve per question. Higher recall
    // costs more prompt tokens; 4 is enough grounding for course/fee/FAQ
    // style questions without drowning the model in irrelevant chunks.
    private static final int RAG_TOP_K = 4;

    private final ChatClient chatClient;
    private final RagRetrievalService ragRetrievalService;
    private final EnrollmentService enrollmentService;
    private final StudentCourseService studentCourseService;
    private final StudentProgramService studentProgramService;
    private final StudentRepository studentRepository;
    private final AssistantChatMessageRepository chatMessageRepository;

    public EnrollmentAssistantServiceImpl(
            ChatClient studentAssistantChatClient,
            RagRetrievalService ragRetrievalService,
            EnrollmentService enrollmentService,
            StudentCourseService studentCourseService,
            StudentProgramService studentProgramService,
            StudentRepository studentRepository,
            AssistantChatMessageRepository chatMessageRepository) {
        this.chatClient = studentAssistantChatClient;
        this.ragRetrievalService = ragRetrievalService;
        this.enrollmentService = enrollmentService;
        this.studentCourseService = studentCourseService;
        this.studentProgramService = studentProgramService;
        this.studentRepository = studentRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public AssistantChatResponse chat(AssistantChatRequest request, String authenticatedStudentId) {
        // sessionId/conversationId are the same concept here - see
        // ConversationIdSupport for why the id itself encodes ownership.
        String conversationId = resolveConversationId(request.getConversationId(), authenticatedStudentId);

        try {
            AssistantTools.setStudentId(authenticatedStudentId);

            // --- RAG retrieval step ---
            // Pull the most relevant static knowledge (course/program/fee/
            // schedule/FAQ text) out of the vector store for this specific
            // question. See RagRetrievalService for how "relevant" is scored
            // (embedding similarity search + a keyword-overlap rerank).
            //
            // Retrieval is treated as best-effort, not a hard dependency: if
            // Elasticsearch is unreachable (not yet stood up, network blip),
            // the assistant still answers using tools + chat memory alone
            // instead of failing the whole request. The system prompt's
            // grounding rules already tell it to say so rather than guess
            // when no knowledge-base context was found.
            String contextBlock = safeRetrieveContext(request.getMessage());

            // The retrieved context is injected as this turn's system prompt
            // (base grounding rules + context). Live/per-student data (the
            // other half of RAG+tools) is handled separately: defaultTools on
            // the ChatClient bean lets the model call AssistantTools in the
            // same turn when the question needs real-time data instead of
            // knowledge-base text. Chat memory (previous turns) is added
            // automatically by the MessageChatMemoryAdvisor configured on the
            // client, keyed by conversationId below.
            org.springframework.ai.chat.model.ChatResponse chatResponseTmp = chatClient.prompt()
                    .system(AssistantSystemPrompt.withContext(contextBlock))
                    .user(request.getMessage())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .chatResponse();

            String responseText = chatResponseTmp.getResult().getOutput().getText();

            return new AssistantChatResponse(responseText, conversationId);

        } catch (Exception e) {
            logger.error("Error in student assistant chat", e);
            throw new RuntimeException("Assistant encountered an error: " + e.getMessage());
        } finally {
            AssistantTools.clear();
        }
    }

    @Override
    public List<ChatMessageResponse> getHistory(String conversationId, String authenticatedStudentId) {
        if (!ConversationIdSupport.belongsTo(conversationId, authenticatedStudentId)) {
            throw new UnauthorizedAccessException("This chat session does not belong to the authenticated student.");
        }

        return chatMessageRepository.findByConversationIdOrderBySequenceNoAsc(conversationId).stream()
                .map(row -> new ChatMessageResponse(
                        row.getMessageType().name(),
                        row.getContent(),
                        row.getCreatedDt()))
                .collect(Collectors.toList());
    }

    private String resolveConversationId(String requestedConversationId, String authenticatedStudentId) {
        // A blank id, or one that doesn't even parse to <studentId>::<uuid>
        // (e.g. a client/Swagger placeholder like "string"), can't possibly
        // be a real session of anyone's - treat it the same as "none given"
        // and start a fresh conversation instead of rejecting the request.
        if (requestedConversationId == null
                || requestedConversationId.isBlank()
                || ConversationIdSupport.studentIdOf(requestedConversationId) == null) {
            return ConversationIdSupport.newConversationId(authenticatedStudentId);
        }
        if (!ConversationIdSupport.belongsTo(requestedConversationId, authenticatedStudentId)) {
            throw new UnauthorizedAccessException("This chat session does not belong to the authenticated student.");
        }
        return requestedConversationId;
    }

    private String safeRetrieveContext(String message) {
        try {
            List<Document> retrieved = ragRetrievalService.retrieve(message, RAG_TOP_K);
            return buildContextBlock(retrieved);
        } catch (Exception e) {
            logger.warn("Knowledge base retrieval unavailable (is Elasticsearch running?): {}", e.getMessage());
            return "";
        }
    }

    private String buildContextBlock(List<Document> documents) {
        if (documents.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Document document : documents) {
            Object sourceType = document.getMetadata().get("sourceType");
            Object sourceId = document.getMetadata().get("sourceId");
            sb.append(String.format("[%d] (%s %s)%n%s%n%n", i++, sourceType, sourceId, document.getText()));
        }
        return sb.toString();
    }

    @Override
    public EnrollmentConfirmResponse confirmEnrollment(
            EnrollmentConfirmRequest request,
            String authenticatedStudentId) {

        try {
            studentRepository.findByStudentId(authenticatedStudentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            if (request.getCourseId() != null && !request.getCourseId().isBlank()) {
                return confirmCourseEnrollment(authenticatedStudentId, request.getCourseId());
            } else if (request.getProgramId() != null && !request.getProgramId().isBlank()) {
                return confirmProgramEnrollment(authenticatedStudentId, request.getProgramId());
            } else {
                EnrollmentConfirmResponse resp = new EnrollmentConfirmResponse();
                resp.setSuccess(false);
                resp.setMessage("Please provide either a course ID or program ID for enrollment.");
                return resp;
            }
        } catch (Exception e) {
            logger.error("Error confirming enrollment", e);
            EnrollmentConfirmResponse resp = new EnrollmentConfirmResponse();
            resp.setSuccess(false);
            resp.setMessage("Enrollment failed: " + e.getMessage());
            return resp;
        }
    }

    private EnrollmentConfirmResponse confirmCourseEnrollment(String studentId, String courseId) {
        boolean alreadyEnrolled = isAlreadyEnrolledInCourse(studentId, courseId);
        if (alreadyEnrolled) {
            EnrollmentConfirmResponse resp = new EnrollmentConfirmResponse();
            resp.setSuccess(false);
            resp.setMessage("You are already enrolled in this course.");
            return resp;
        }

        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setStudentId(studentId);
        enrollmentRequest.setCourseId(courseId);

        var enrollmentResponse = enrollmentService.createEnrollment(enrollmentRequest);

        StudentCourseEnrollRequest studentCourseRequest = new StudentCourseEnrollRequest();
        studentCourseRequest.setStudentId(studentId);
        studentCourseRequest.setCourseId(courseId);
        studentCourseService.enroll(studentCourseRequest);

        EnrollmentConfirmResponse resp = new EnrollmentConfirmResponse();
        resp.setSuccess(true);
        resp.setMessage("Successfully enrolled in course: " + courseId);
        resp.setEnrollmentId(enrollmentResponse.getId());
        return resp;
    }

    private EnrollmentConfirmResponse confirmProgramEnrollment(String studentId, String programId) {
        boolean alreadyEnrolled = isAlreadyEnrolledInProgram(studentId, programId);
        if (alreadyEnrolled) {
            EnrollmentConfirmResponse resp = new EnrollmentConfirmResponse();
            resp.setSuccess(false);
            resp.setMessage("You are already enrolled in this program.");
            return resp;
        }

        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setStudentId(studentId);
        enrollmentRequest.setProgramId(programId);

        var enrollmentResponse = enrollmentService.createEnrollment(enrollmentRequest);

        AssignProgramRequest assignProgramRequest = new AssignProgramRequest();
        assignProgramRequest.setStudentId(studentId);
        assignProgramRequest.setProgramId(programId);
        studentProgramService.assignProgramToStudent(assignProgramRequest);

        EnrollmentConfirmResponse resp = new EnrollmentConfirmResponse();
        resp.setSuccess(true);
        resp.setMessage("Successfully enrolled in program: " + programId);
        resp.setEnrollmentId(enrollmentResponse.getId());
        return resp;
    }

    private boolean isAlreadyEnrolledInCourse(String studentId, String courseId) {
        return enrollmentService.getEnrollmentsByStudent(studentId).stream()
                .anyMatch(e -> courseId.equals(e.getCourseId())
                        && e.getStatus() != null
                        && !"CANCELLED".equals(e.getStatus().name()));
    }

    private boolean isAlreadyEnrolledInProgram(String studentId, String programId) {
        return enrollmentService.getEnrollmentsByStudent(studentId).stream()
                .anyMatch(e -> programId.equals(e.getProgramId())
                        && e.getStatus() != null
                        && !"CANCELLED".equals(e.getStatus().name()));
    }
}
