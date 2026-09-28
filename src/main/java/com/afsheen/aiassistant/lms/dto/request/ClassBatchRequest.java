package com.afsheen.aiassistant.lms.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public class ClassBatchRequest {

    @NotBlank
    private String courseId;

    @NotBlank
    private String className;

    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer capacity;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
