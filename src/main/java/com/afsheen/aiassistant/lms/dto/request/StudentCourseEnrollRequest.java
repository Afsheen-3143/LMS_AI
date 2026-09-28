package com.afsheen.aiassistant.lms.dto.request;

public class StudentCourseEnrollRequest {

    private String studentId;
    private String courseId;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
}
