package com.afsheen.aiassistant.rag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.entity.AssistantFaq;
import com.afsheen.aiassistant.lms.dto.response.CourseFeeSettingResponse;
import com.afsheen.aiassistant.lms.dto.response.CourseResponse;
import com.afsheen.aiassistant.lms.dto.response.ProgramFeeSettingResponse;
import com.afsheen.aiassistant.lms.dto.response.ProgramResponse;
import com.afsheen.aiassistant.lms.entity.ClassBatch;
import com.afsheen.aiassistant.lms.repository.ClassBatchRepository;
import com.afsheen.aiassistant.lms.repository.ClassScheduleRepository;
import com.afsheen.aiassistant.lms.service.CourseFeeService;
import com.afsheen.aiassistant.lms.service.CourseManagementService;
import com.afsheen.aiassistant.lms.service.ProgramFeeService;
import com.afsheen.aiassistant.repository.AssistantFaqRepository;

/**
 * Builds the RAG knowledge base: pulls the LMS's static/slow-changing
 * content (course catalog, fees, batch schedules, FAQs), chunks it, embeds
 * it, and writes it into the {@link VectorStore}.
 *
 * <p><b>Chunking strategy - why size matters:</b> each embedding vector
 * represents the meaning of the *whole* chunk it's built from. A chunk that's
 * too large (e.g. a full course catalog) gets an averaged-out, blurry
 * embedding that matches poorly against specific questions. A chunk that's
 * too small (e.g. one sentence) loses the surrounding context needed to
 * answer anything but the narrowest question. {@link TokenTextSplitter} here
 * targets ~800-token chunks at sentence boundaries, which comfortably holds
 * one course/program/FAQ's worth of context without diluting it.
 *
 * <p>This is intentionally the *only* place that talks to the vector store
 * for writes - {@link RagRetrievalService} only ever reads from it.
 */
@Service
public class DocumentIngestionService {

    private static final Logger logger = LogManager.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;
    private final CourseManagementService courseManagementService;
    private final CourseFeeService courseFeeService;
    private final ProgramFeeService programFeeService;
    private final ClassBatchRepository classBatchRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final AssistantFaqRepository assistantFaqRepository;

    public DocumentIngestionService(
            VectorStore vectorStore,
            CourseManagementService courseManagementService,
            CourseFeeService courseFeeService,
            ProgramFeeService programFeeService,
            ClassBatchRepository classBatchRepository,
            ClassScheduleRepository classScheduleRepository,
            AssistantFaqRepository assistantFaqRepository) {
        this.vectorStore = vectorStore;
        this.textSplitter = new TokenTextSplitter();
        this.courseManagementService = courseManagementService;
        this.courseFeeService = courseFeeService;
        this.programFeeService = programFeeService;
        this.classBatchRepository = classBatchRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.assistantFaqRepository = assistantFaqRepository;
    }

    /**
     * Rebuilds the entire knowledge base from scratch. Simple and correct for
     * this data volume (hundreds, not millions, of source rows); an
     * incremental/delta ingestion would be the next step if the catalog grows
     * large enough for a full reindex to become slow.
     */
    public IngestionResult reindexAll() {
        List<Document> sourceDocuments = new ArrayList<>();
        sourceDocuments.addAll(buildCourseDocuments());
        sourceDocuments.addAll(buildProgramDocuments());
        sourceDocuments.addAll(buildBatchScheduleDocuments());
        sourceDocuments.addAll(buildFaqDocuments());

        List<Document> chunks = textSplitter.apply(sourceDocuments);

        // VectorStore.add() embeds each chunk (via the configured
        // EmbeddingModel) and writes the vector + original text + metadata
        // into Elasticsearch in one call.
        vectorStore.add(chunks);

        logger.info("Assistant knowledge base reindexed: {} source documents -> {} chunks",
                sourceDocuments.size(), chunks.size());

        return new IngestionResult(sourceDocuments.size(), chunks.size());
    }

    private List<Document> buildCourseDocuments() {
        return courseManagementService.viewAllCourses().stream()
                .map(this::toCourseDocument)
                .collect(Collectors.toList());
    }

    private Document toCourseDocument(CourseResponse course) {
        String feeInfo = "Fee information not yet configured.";
        try {
            CourseFeeSettingResponse fee = courseFeeService.getCourseFeeSetting(course.getCourseId());
            if (fee.getCurrentFee() != null) {
                feeInfo = String.format(
                        "Current fee: %s (discount: %s). Duration: %s.",
                        fee.getCurrentFee().getFee(),
                        fee.getCurrentFee().getDiscount(),
                        fee.getCourseDuration());
            }
        } catch (Exception ex) {
            // No fee configured for this course yet - do not fabricate one,
            // the assistant will honestly say fee info is unavailable.
            logger.debug("No fee data for course {}: {}", course.getCourseId(), ex.getMessage());
        }

        String content = """
                Course: %s (Course ID: %s)
                Subject: %s
                Level: %s
                Language: %s
                Skills covered: %s
                Description: %s
                %s
                """.formatted(
                course.getCourseTitle(),
                course.getCourseId(),
                course.getSubjectNm(),
                course.getLevel(),
                course.getLanguage(),
                course.getSkills() != null ? String.join(", ", course.getSkills()) : "N/A",
                course.getDescription(),
                feeInfo);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", "COURSE");
        metadata.put("sourceId", course.getCourseId());
        metadata.put("category", course.getSubjectNm() != null ? course.getSubjectNm() : "General");

        return new Document(content, metadata);
    }

    private List<Document> buildProgramDocuments() {
        return courseManagementService.getAllPrograms().stream()
                .map(this::toProgramDocument)
                .collect(Collectors.toList());
    }

    private Document toProgramDocument(ProgramResponse program) {
        String feeInfo = "Fee information not yet configured.";
        try {
            ProgramFeeSettingResponse fee = programFeeService.getProgramFeeSetting(program.getProgramId());
            if (fee.getCurrentFee() != null) {
                feeInfo = String.format(
                        "Current fee: %s (discount: %s). Duration: %s.",
                        fee.getCurrentFee().getFee(),
                        fee.getCurrentFee().getDiscount(),
                        fee.getDuration());
            }
        } catch (Exception ex) {
            logger.debug("No fee data for program {}: {}", program.getProgramId(), ex.getMessage());
        }

        String courseTitles = program.getCoursesList() == null ? "N/A"
                : program.getCoursesList().stream()
                        .map(CourseResponse::getCourseTitle)
                        .collect(Collectors.joining(", "));

        String content = """
                Program: %s (Program ID: %s)
                Description: %s
                Included courses: %s
                %s
                """.formatted(
                program.getProgramTitle(),
                program.getProgramId(),
                program.getDescription(),
                courseTitles,
                feeInfo);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", "PROGRAM");
        metadata.put("sourceId", program.getProgramId());
        metadata.put("category", "Program");

        return new Document(content, metadata);
    }

    private List<Document> buildBatchScheduleDocuments() {
        return courseManagementService.viewAllCourses().stream()
                .flatMap(course -> classBatchRepository.findByCourse_CourseId(course.getCourseId()).stream()
                        .map(batch -> toBatchDocument(course, batch)))
                .collect(Collectors.toList());
    }

    private Document toBatchDocument(CourseResponse course, ClassBatch batch) {
        String scheduleSummary = classScheduleRepository.findByClassBatch_Id(batch.getId()).stream()
                .map(schedule -> String.format(
                        "%s: %s %s-%s (%s)",
                        schedule.getClassDate(),
                        schedule.getClassName(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getMode()))
                .collect(Collectors.joining("; "));

        String content = """
                Batch: %s for course %s (Course ID: %s)
                Start date: %s, End date: %s
                Capacity: %s
                Status: %s
                Weekly schedule: %s
                """.formatted(
                batch.getClassName(),
                course.getCourseTitle(),
                course.getCourseId(),
                batch.getStartDate(),
                batch.getEndDate(),
                batch.getCapacity() != null ? batch.getCapacity() : "Unlimited",
                batch.getStatus() != null ? batch.getStatus() : "ACTIVE",
                scheduleSummary.isBlank() ? "Not yet scheduled" : scheduleSummary);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", "BATCH_SCHEDULE");
        metadata.put("sourceId", String.valueOf(batch.getId()));
        metadata.put("category", course.getCourseId());

        return new Document(content, metadata);
    }

    private List<Document> buildFaqDocuments() {
        return assistantFaqRepository.findByActiveTrue().stream()
                .map(this::toFaqDocument)
                .collect(Collectors.toList());
    }

    private Document toFaqDocument(AssistantFaq faq) {
        String content = "Q: " + faq.getQuestion() + "\nA: " + faq.getAnswer();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", "FAQ");
        metadata.put("sourceId", String.valueOf(faq.getId()));
        metadata.put("category", faq.getCategory());

        return new Document(content, metadata);
    }

    public record IngestionResult(int sourceDocuments, int chunks) {
    }
}
