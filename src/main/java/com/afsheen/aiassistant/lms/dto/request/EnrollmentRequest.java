package com.afsheen.aiassistant.lms.dto.request;

public class EnrollmentRequest {

    private String studentId;
    private String courseId;
    private String programId;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }
}
