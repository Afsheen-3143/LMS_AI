package com.afsheen.aiassistant.lms.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public class ClassScheduleRequest {

    @NotNull
    private Long classBatchId;

    private String className;
    private LocalDate classDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String mode;
    private String status;

    public Long getClassBatchId() { return classBatchId; }
    public void setClassBatchId(Long classBatchId) { this.classBatchId = classBatchId; }

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
