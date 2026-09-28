package com.afsheen.aiassistant.lms.service;

import java.util.List;

import com.afsheen.aiassistant.lms.dto.request.StudentRegistrationRequest;
import com.afsheen.aiassistant.lms.dto.response.StudentResponse;

public interface StudentService {

    StudentResponse getStudentById(String studentId);

    List<StudentResponse> getAllStudents();

    StudentResponse registerStudent(StudentRegistrationRequest request);

    StudentResponse updateStudent(String studentId, StudentRegistrationRequest request);

    void deleteStudent(String studentId);
}
