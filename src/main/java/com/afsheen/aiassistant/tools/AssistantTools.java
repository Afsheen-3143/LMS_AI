package com.afsheen.aiassistant.tools;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.afsheen.aiassistant.lms.dto.response.CourseFeeSettingResponse;
import com.afsheen.aiassistant.lms.dto.response.CourseResponse;
import com.afsheen.aiassistant.lms.dto.response.EnrollmentBatchResponse;
import com.afsheen.aiassistant.lms.dto.response.EnrollmentResponse;
import com.afsheen.aiassistant.lms.dto.response.ProgramFeeSettingResponse;
import com.afsheen.aiassistant.lms.dto.response.ProgramResponse;
import com.afsheen.aiassistant.lms.entity.ClassBatch;
import com.afsheen.aiassistant.lms.repository.ClassBatchRepository;
import com.afsheen.aiassistant.lms.repository.ClassScheduleRepository;
import com.afsheen.aiassistant.lms.repository.ClassStudentRepository;
import com.afsheen.aiassistant.lms.service.CourseFeeService;
import com.afsheen.aiassistant.lms.service.CourseManagementService;
import com.afsheen.aiassistant.lms.service.EnrollmentBatchService;
import com.afsheen.aiassistant.lms.service.EnrollmentService;
import com.afsheen.aiassistant.lms.service.ProgramFeeService;
import com.afsheen.aiassistant.lms.service.StudentService;

/**
 * Tool calling handles everything here: data that is live/per-student and
 * would either go stale in the vector store (seat counts, payment status) or
 * must never be shared across students (a specific student's own
 * enrollments). RAG (see {@code rag}) handles the opposite case: static,
 * shared knowledge - course catalog text, fee schedules, FAQs - that's fine
 * to pre-embed and search semantically. The model decides per-question which
 * of these @Tool methods (if any) it needs to call.
 */
@Component
public class AssistantTools {

    private static final ThreadLocal<String> CURRENT_STUDENT_ID = new ThreadLocal<>();

    private final CourseManagementService courseManagementService;
    private final EnrollmentService enrollmentService;
    private final EnrollmentBatchService enrollmentBatchService;
    private final CourseFeeService courseFeeService;
    private final ProgramFeeService programFeeService;
    private final StudentService studentService;
    private final ClassBatchRepository classBatchRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final ClassStudentRepository classStudentRepository;

    public AssistantTools(
            CourseManagementService courseManagementService,
            EnrollmentService enrollmentService,
            EnrollmentBatchService enrollmentBatchService,
            CourseFeeService courseFeeService,
            ProgramFeeService programFeeService,
            StudentService studentService,
            ClassBatchRepository classBatchRepository,
            ClassScheduleRepository classScheduleRepository,
            ClassStudentRepository classStudentRepository) {
        this.courseManagementService = courseManagementService;
        this.enrollmentService = enrollmentService;
        this.enrollmentBatchService = enrollmentBatchService;
        this.courseFeeService = courseFeeService;
        this.programFeeService = programFeeService;
        this.studentService = studentService;
        this.classBatchRepository = classBatchRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.classStudentRepository = classStudentRepository;
    }

    public static void setStudentId(String studentId) {
        CURRENT_STUDENT_ID.set(studentId);
    }

    public static void clear() {
        CURRENT_STUDENT_ID.remove();
    }

    private String getStudentId() {
        String id = CURRENT_STUDENT_ID.get();
        if (id == null || id.isBlank()) {
            throw new RuntimeException("Student not authenticated. Please log in again.");
        }
        return id;
    }

    // ==================== COURSE TOOLS ====================

    @Tool(description = "Get all currently available courses in the LMS. Use this when the student wants to browse or explore all courses.")
    public List<CourseResponse> getAllCourses() {
        return courseManagementService.viewAllCourses();
    }

    @Tool(description = "Search courses by keyword, topic, skill or interest. Use this when the student mentions a topic like 'Java', 'backend', 'web development', 'data science', 'Spring Boot', etc. Match against course title, skills, and description.")
    public List<CourseResponse> searchCourses(
            @ToolParam(description = "The keyword or topic to search for, e.g. 'Java', 'backend', 'web development'") String keyword) {
        List<CourseResponse> allCourses = courseManagementService.viewAllCourses();
        String lowerKeyword = keyword.toLowerCase();
        return allCourses.stream()
                .filter(course -> {
                    boolean titleMatch = course.getCourseTitle() != null
                            && course.getCourseTitle().toLowerCase().contains(lowerKeyword);
                    boolean descMatch = course.getDescription() != null
                            && course.getDescription().toLowerCase().contains(lowerKeyword);
                    boolean skillMatch = course.getSkills() != null
                            && course.getSkills().stream().anyMatch(
                                    skill -> skill.toLowerCase().contains(lowerKeyword));
                    boolean subjectMatch = course.getSubjectNm() != null
                            && course.getSubjectNm().toLowerCase().contains(lowerKeyword);
                    return titleMatch || descMatch || skillMatch || subjectMatch;
                })
                .collect(Collectors.toList());
    }

    @Tool(description = "Get complete details of a specific course including title, description, language, level, and skills. Use the courseId string like 'CRS001'.")
    public CourseResponse getCourseDetails(
            @ToolParam(description = "The course ID string, e.g. 'CRS001'") String courseId) {
        return courseManagementService.viewAllCourses().stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
    }

    // ==================== PROGRAM TOOLS ====================

    @Tool(description = "Get all available learning programs in the LMS.")
    public List<ProgramResponse> getAllPrograms() {
        return courseManagementService.getAllPrograms();
    }

    @Tool(description = "Search programs by keyword or topic. Use this when the student is interested in a structured program rather than individual courses.")
    public List<ProgramResponse> searchPrograms(
            @ToolParam(description = "The keyword or topic to search for in programs") String keyword) {
        List<ProgramResponse> allPrograms = courseManagementService.getAllPrograms();
        String lowerKeyword = keyword.toLowerCase();
        return allPrograms.stream()
                .filter(program -> {
                    boolean titleMatch = program.getProgramTitle() != null
                            && program.getProgramTitle().toLowerCase().contains(lowerKeyword);
                    boolean descMatch = program.getDescription() != null
                            && program.getDescription().toLowerCase().contains(lowerKeyword);
                    boolean courseMatch = program.getCoursesList() != null
                            && program.getCoursesList().stream().anyMatch(
                                    course -> course.getCourseTitle() != null
                                            && course.getCourseTitle().toLowerCase().contains(lowerKeyword));
                    return titleMatch || descMatch || courseMatch;
                })
                .collect(Collectors.toList());
    }

    @Tool(description = "Get complete details of a specific program including its title, description, duration, and list of included courses.")
    public ProgramResponse getProgramDetails(
            @ToolParam(description = "The program ID string, e.g. 'PRG001'") String programId) {
        List<ProgramResponse> programs = courseManagementService.getAllPrograms();
        return programs.stream()
                .filter(p -> p.getProgramId().equals(programId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Program not found with ID: " + programId));
    }

    @Tool(description = "Get all courses that belong to a specific program. Use this to show which courses are included in a program.")
    public List<CourseResponse> getProgramCourses(
            @ToolParam(description = "The program ID string, e.g. 'PRG001'") String programId) {
        ProgramResponse program = getProgramDetails(programId);
        return program.getCoursesList() != null ? program.getCoursesList() : List.of();
    }

    // ==================== BATCH TOOLS ====================

    @Tool(description = "Get available batches (class groups) for a specific course. Shows batch name, start date, end date, capacity and status.")
    public List<String> getAvailableBatches(
            @ToolParam(description = "The course ID string, e.g. 'CRS001'") String courseId) {
        return classBatchRepository.findByCourse_CourseId(courseId).stream()
                .map(batch -> String.format(
                        "Batch ID: %d | Name: %s | Start: %s | End: %s | Capacity: %s | Status: %s",
                        batch.getId(),
                        batch.getClassName(),
                        batch.getStartDate(),
                        batch.getEndDate(),
                        batch.getCapacity() != null ? batch.getCapacity() : "Unlimited",
                        batch.getStatus() != null ? batch.getStatus() : "Active"))
                .collect(Collectors.toList());
    }

    @Tool(description = "Get the weekly class schedule for a specific batch. Shows class names, dates, times, instructors, and meeting links.")
    public List<String> getBatchSchedule(
            @ToolParam(description = "The batch ID as a number") Long batchId) {
        return classScheduleRepository.findByClassBatch_Id(batchId).stream()
                .map(schedule -> String.format(
                        "Class: %s | Date: %s | Time: %s-%s | Mode: %s | Status: %s",
                        schedule.getClassName(),
                        schedule.getClassDate(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getMode(),
                        schedule.getStatus()))
                .collect(Collectors.toList());
    }

    // ==================== ENROLLMENT TOOLS ====================

    @Tool(description = "Get the currently authenticated student's enrollments. Shows enrolled courses/programs with status and payment status.")
    public List<EnrollmentResponse> getMyEnrollments() {
        String studentId = getStudentId();
        return enrollmentService.getEnrollmentsByStudent(studentId);
    }

    @Tool(description = "Check enrollment status for the currently authenticated student in a specific course. Provide the courseId.")
    public String checkCourseEnrollmentStatus(
            @ToolParam(description = "The course ID to check, e.g. 'CRS001'") String courseId) {
        String studentId = getStudentId();
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        return enrollments.stream()
                .filter(e -> courseId.equals(e.getCourseId()))
                .findFirst()
                .map(e -> String.format("Enrolled in %s | Status: %s | Payment: %s",
                        e.getCourseTitle(), e.getStatus(), e.getPaymentStatus()))
                .orElse("You are not enrolled in course: " + courseId);
    }

    @Tool(description = "Get the student's enrolled batches and their details.")
    public List<EnrollmentBatchResponse> getMyBatches() {
        String studentId = getStudentId();
        return enrollmentBatchService.getEnrolledBatchesByStudentId(studentId);
    }

    // ==================== FEE TOOLS ====================

    @Tool(description = "Get the fee details for a specific course including current fee, discount, and duration.")
    public String getCourseFee(
            @ToolParam(description = "The course ID string, e.g. 'CRS001'") String courseId) {
        CourseFeeSettingResponse fee = courseFeeService.getCourseFeeSetting(courseId);
        if (fee.getCurrentFee() == null) {
            return "No fee information available for course: " + courseId;
        }
        return String.format("Course: %s | Fee: %s | Discount: %s | Duration: %s",
                fee.getCourseTitle(),
                fee.getCurrentFee().getFee(),
                fee.getCurrentFee().getDiscount(),
                fee.getCourseDuration());
    }

    @Tool(description = "Get the fee details for a specific program including current fee, discount, and duration.")
    public String getProgramFee(
            @ToolParam(description = "The program ID string, e.g. 'PRG001'") String programId) {
        ProgramFeeSettingResponse fee = programFeeService.getProgramFeeSetting(programId);
        if (fee.getCurrentFee() == null) {
            return "No fee information available for program: " + programId;
        }
        return String.format("Program: %s | Fee: %s | Discount: %s | Duration: %s",
                fee.getProgramTitle(),
                fee.getCurrentFee().getFee(),
                fee.getCurrentFee().getDiscount(),
                fee.getDuration());
    }

    // ==================== STUDENT INFO TOOLS ====================

    @Tool(description = "Get the currently authenticated student's basic profile information.")
    public String getMyProfile() {
        String studentId = getStudentId();
        var student = studentService.getStudentById(studentId);
        return String.format("Name: %s %s | Student ID: %s | Email: %s | Phone: %s",
                student.getFirstNm(),
                student.getLastNm(),
                student.getStudentId(),
                student.getEmailId(),
                student.getMobileNum());
    }

    @Tool(description = "Check if the student is already enrolled in a specific course. Returns true if enrolled, false otherwise.")
    public boolean isEnrolledInCourse(
            @ToolParam(description = "The course ID to check, e.g. 'CRS001'") String courseId) {
        String studentId = getStudentId();
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        return enrollments.stream()
                .anyMatch(e -> courseId.equals(e.getCourseId()) && e.getStatus() != null
                        && e.getStatus().name() != "CANCELLED");
    }

    // ==================== LIVE / DYNAMIC DATA TOOLS ====================
    // These deliberately bypass the vector store: seat counts change on every
    // enrollment and payment status is per-student, so pre-embedding either
    // would go stale or leak one student's data into another's search
    // results. The model calls these instead whenever a question needs a
    // real-time number rather than descriptive knowledge-base text.

    @Tool(description = "Check real-time seat availability for a specific batch: how many seats are taken vs the batch capacity. Use the batchId as a number.")
    public String getSeatAvailability(
            @ToolParam(description = "The batch ID as a number") Long batchId) {
        ClassBatch batch = classBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        if (batch.getCapacity() == null) {
            return String.format("Batch '%s' has no seat limit (unlimited capacity).", batch.getClassName());
        }

        long enrolledCount = classStudentRepository.countByClassBatchId(batchId);
        long seatsLeft = Math.max(0, batch.getCapacity() - enrolledCount);

        return String.format(
                "Batch '%s': %d of %d seats filled, %d seat(s) remaining.",
                batch.getClassName(), enrolledCount, batch.getCapacity(), seatsLeft);
    }

    @Tool(description = "Get the currently authenticated student's payment status for all their enrollments (e.g. PAID, PENDING). "
            + "Note: this reports payment status only - the LMS does not currently track a specific fee due date per enrollment, "
            + "so never invent one; if asked for a due date, say that only payment status is available.")
    public List<String> getMyPaymentStatus() {
        String studentId = getStudentId();
        return enrollmentService.getEnrollmentsByStudent(studentId).stream()
                .map(e -> String.format("%s: payment status = %s",
                        e.getCourseTitle() != null ? e.getCourseTitle() : e.getProgramId(),
                        e.getPaymentStatus() != null ? e.getPaymentStatus() : "UNKNOWN"))
                .collect(Collectors.toList());
    }
}
