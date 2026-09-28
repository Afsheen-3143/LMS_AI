package com.afsheen.aiassistant.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One seat taken by a student in a batch - backs real-time seat counting. */
@Entity
@Table(name = "class_student")
public class ClassStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_batch_id", nullable = false)
    private Long classBatchId;

    @Column(name = "student_id", nullable = false, length = 10)
    private String studentId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClassBatchId() { return classBatchId; }
    public void setClassBatchId(Long classBatchId) { this.classBatchId = classBatchId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
}
