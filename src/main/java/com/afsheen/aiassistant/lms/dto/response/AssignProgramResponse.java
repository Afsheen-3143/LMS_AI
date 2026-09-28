package com.afsheen.aiassistant.lms.dto.response;

/** Marker return type - the assignProgramToStudent() call site never reads its fields. */
public class AssignProgramResponse {

    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
