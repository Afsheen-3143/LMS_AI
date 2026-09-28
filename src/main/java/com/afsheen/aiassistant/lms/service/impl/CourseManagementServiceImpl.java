package com.afsheen.aiassistant.lms.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.exceptions.ResourceNotFoundException;
import com.afsheen.aiassistant.lms.dto.request.CourseRequest;
import com.afsheen.aiassistant.lms.dto.response.CourseResponse;
import com.afsheen.aiassistant.lms.dto.response.ProgramResponse;
import com.afsheen.aiassistant.lms.entity.Course;
import com.afsheen.aiassistant.lms.entity.Program;
import com.afsheen.aiassistant.lms.entity.ProgramCourse;
import com.afsheen.aiassistant.lms.repository.CourseRepository;
import com.afsheen.aiassistant.lms.repository.ProgramCourseRepository;
import com.afsheen.aiassistant.lms.repository.ProgramRepository;
import com.afsheen.aiassistant.lms.service.CourseManagementService;

@Service
public class CourseManagementServiceImpl implements CourseManagementService {

    private final CourseRepository courseRepository;
    private final ProgramRepository programRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final IdSequenceGenerator idSequenceGenerator;

    public CourseManagementServiceImpl(
            CourseRepository courseRepository,
            ProgramRepository programRepository,
            ProgramCourseRepository programCourseRepository,
            IdSequenceGenerator idSequenceGenerator) {
        this.courseRepository = courseRepository;
        this.programRepository = programRepository;
        this.programCourseRepository = programCourseRepository;
        this.idSequenceGenerator = idSequenceGenerator;
    }

    @Override
    public List<CourseResponse> viewAllCourses() {
        return courseRepository.findAll().stream().map(CourseManagementServiceImpl::toResponse).toList();
    }

    @Override
    public CourseResponse getCourseById(String courseId) {
        return toResponse(findCourse(courseId));
    }

    @Override
    public CourseResponse createCourse(CourseRequest request) {
        Course course = new Course();
        course.setCourseId(idSequenceGenerator.next("COURSE", "CRS", 3));
        applyRequest(course, request);
        return toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse updateCourse(String courseId, CourseRequest request) {
        Course course = findCourse(courseId);
        applyRequest(course, request);
        return toResponse(courseRepository.save(course));
    }

    @Override
    public void deleteCourse(String courseId) {
        courseRepository.delete(findCourse(courseId));
    }

    @Override
    public List<ProgramResponse> getAllPrograms() {
        return programRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public ProgramResponse getProgramById(String programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found: " + programId));
        return toResponse(program);
    }

    private Course findCourse(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    }

    private static void applyRequest(Course course, CourseRequest request) {
        course.setCourseTitle(request.getCourseTitle());
        course.setSubjectNm(request.getSubjectNm());
        course.setDescription(request.getDescription());
        course.setLanguage(request.getLanguage());
        course.setLevel(request.getLevel());
        course.setSkills(request.getSkills() != null ? String.join(",", request.getSkills()) : null);
    }

    private static CourseResponse toResponse(Course c) {
        CourseResponse r = new CourseResponse();
        r.setCourseId(c.getCourseId());
        r.setCourseTitle(c.getCourseTitle());
        r.setDescription(c.getDescription());
        r.setLanguage(c.getLanguage());
        r.setSubjectNm(c.getSubjectNm());
        r.setLevel(c.getLevel());
        r.setSkills(c.getSkills() != null && !c.getSkills().isBlank()
                ? Arrays.asList(c.getSkills().split("\\s*,\\s*"))
                : List.of());
        return r;
    }

    private ProgramResponse toResponse(Program p) {
        ProgramResponse r = new ProgramResponse();
        r.setProgramId(p.getProgramId());
        r.setProgramTitle(p.getProgramTitle());
        r.setDescription(p.getDescription());
        r.setCoursesList(programCourseRepository.findByProgram_ProgramIdOrderBySortOrderAsc(p.getProgramId())
                .stream()
                .map(ProgramCourse::getCourse)
                .map(CourseManagementServiceImpl::toResponse)
                .toList());
        return r;
    }
}
