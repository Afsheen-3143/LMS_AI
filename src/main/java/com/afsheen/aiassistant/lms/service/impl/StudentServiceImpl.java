package com.afsheen.aiassistant.lms.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.exceptions.ResourceNotFoundException;
import com.afsheen.aiassistant.lms.dto.request.StudentRegistrationRequest;
import com.afsheen.aiassistant.lms.dto.response.StudentResponse;
import com.afsheen.aiassistant.lms.entity.Student;
import com.afsheen.aiassistant.lms.repository.StudentRepository;
import com.afsheen.aiassistant.lms.service.StudentService;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final IdSequenceGenerator idSequenceGenerator;

    public StudentServiceImpl(StudentRepository studentRepository, IdSequenceGenerator idSequenceGenerator) {
        this.studentRepository = studentRepository;
        this.idSequenceGenerator = idSequenceGenerator;
    }

    @Override
    public StudentResponse getStudentById(String studentId) {
        return toResponse(findEntity(studentId));
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream().map(StudentServiceImpl::toResponse).toList();
    }

    @Override
    public StudentResponse registerStudent(StudentRegistrationRequest request) {
        Student student = new Student();
        student.setStudentId(idSequenceGenerator.next("STUDENT", "S", 6));
        student.setFirstNm(request.getFirstName());
        student.setLastNm(request.getLastName());
        student.setEmailId(request.getEmail());
        student.setMobileNum(request.getPhone());
        return toResponse(studentRepository.save(student));
    }

    @Override
    public StudentResponse updateStudent(String studentId, StudentRegistrationRequest request) {
        Student student = findEntity(studentId);
        student.setFirstNm(request.getFirstName());
        student.setLastNm(request.getLastName());
        student.setEmailId(request.getEmail());
        student.setMobileNum(request.getPhone());
        return toResponse(studentRepository.save(student));
    }

    @Override
    public void deleteStudent(String studentId) {
        studentRepository.delete(findEntity(studentId));
    }

    private Student findEntity(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
    }

    private static StudentResponse toResponse(Student s) {
        StudentResponse r = new StudentResponse();
        r.setStudentId(s.getStudentId());
        r.setFirstNm(s.getFirstNm());
        r.setLastNm(s.getLastNm());
        r.setEmailId(s.getEmailId());
        r.setMobileNum(s.getMobileNum());
        return r;
    }
}
