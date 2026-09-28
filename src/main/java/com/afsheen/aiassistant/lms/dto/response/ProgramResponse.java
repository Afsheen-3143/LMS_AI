package com.afsheen.aiassistant.lms.dto.response;

import java.util.List;

public class ProgramResponse {

    private String programId;
    private String programTitle;
    private String description;
    private List<CourseResponse> coursesList;

    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }

    public String getProgramTitle() { return programTitle; }
    public void setProgramTitle(String programTitle) { this.programTitle = programTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<CourseResponse> getCoursesList() { return coursesList; }
    public void setCoursesList(List<CourseResponse> coursesList) { this.coursesList = coursesList; }
}
