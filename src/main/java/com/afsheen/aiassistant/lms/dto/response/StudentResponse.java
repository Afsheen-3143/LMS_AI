package com.afsheen.aiassistant.lms.dto.response;

public class StudentResponse {

    private String studentId;
    private String firstNm;
    private String lastNm;
    private String emailId;
    private String mobileNum;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFirstNm() { return firstNm; }
    public void setFirstNm(String firstNm) { this.firstNm = firstNm; }

    public String getLastNm() { return lastNm; }
    public void setLastNm(String lastNm) { this.lastNm = lastNm; }

    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getMobileNum() { return mobileNum; }
    public void setMobileNum(String mobileNum) { this.mobileNum = mobileNum; }
}
