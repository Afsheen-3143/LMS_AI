package com.afsheen.aiassistant.lms.dto.request;

public class AssignProgramRequest {

    private String studentId;
    private String programId;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }
}
