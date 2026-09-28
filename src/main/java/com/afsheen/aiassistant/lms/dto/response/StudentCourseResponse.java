package com.afsheen.aiassistant.lms.dto.response;

/** Marker return type - the enroll() call site never reads its fields. */
public class StudentCourseResponse {

    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
