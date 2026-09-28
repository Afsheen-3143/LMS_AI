package com.afsheen.aiassistant.lms.service;

import java.util.List;

import com.afsheen.aiassistant.lms.dto.request.CourseRequest;
import com.afsheen.aiassistant.lms.dto.response.CourseResponse;
import com.afsheen.aiassistant.lms.dto.response.ProgramResponse;

public interface CourseManagementService {

    List<CourseResponse> viewAllCourses();

    CourseResponse getCourseById(String courseId);

    CourseResponse createCourse(CourseRequest request);

    CourseResponse updateCourse(String courseId, CourseRequest request);

    void deleteCourse(String courseId);

    List<ProgramResponse> getAllPrograms();

    ProgramResponse getProgramById(String programId);
}
