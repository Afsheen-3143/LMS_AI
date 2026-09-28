package com.afsheen.aiassistant.lms.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.afsheen.aiassistant.entity.base.AuditFields;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "class_schedule")
public class ClassSchedule extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_batch_id", nullable = false)
    private ClassBatch classBatch;

    @Column(name = "class_name", nullable = false, length = 200)
    private String className;

    @Column(name = "class_date")
    private LocalDate classDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "mode", length = 30)
    private String mode;

    @Column(name = "status", length = 30)
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ClassBatch getClassBatch() { return classBatch; }
    public void setClassBatch(ClassBatch classBatch) { this.classBatch = classBatch; }

    /** Convenience accessor - most callers only ever need the batch's id, not the whole relation. */
    public Long getClassBatchId() { return classBatch != null ? classBatch.getId() : null; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public LocalDate getClassDate() { return classDate; }
    public void setClassDate(LocalDate classDate) { this.classDate = classDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
