package com.afsheen.aiassistant.lms.entity;

import com.afsheen.aiassistant.entity.base.AuditFields;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "student")
public class Student extends AuditFields {

    @Id
    @Column(name = "student_id", length = 10)
    private String studentId;

    @Column(name = "first_nm", nullable = false, length = 100)
    private String firstNm;

    @Column(name = "last_nm", nullable = false, length = 100)
    private String lastNm;

    @Column(name = "email_id", nullable = false, length = 150, unique = true)
    private String emailId;

    @Column(name = "mobile_num", length = 20)
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
