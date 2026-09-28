package com.afsheen.aiassistant.lms.dto.response;

import java.time.LocalDate;

public class ClassBatchResponse {

    private Long id;
    private String courseId;
    private String courseTitle;
    private String className;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer capacity;
    private long seatsTaken;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

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

    public long getSeatsTaken() { return seatsTaken; }
    public void setSeatsTaken(long seatsTaken) { this.seatsTaken = seatsTaken; }
}
