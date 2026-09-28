package com.afsheen.aiassistant.lms.service;

import com.afsheen.aiassistant.lms.dto.request.StudentCourseEnrollRequest;
import com.afsheen.aiassistant.lms.dto.response.StudentCourseResponse;

public interface StudentCourseService {

    StudentCourseResponse enroll(StudentCourseEnrollRequest request);
}
