package com.afsheen.aiassistant.lms.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public class CourseRequest {

    @NotBlank
    private String courseTitle;

    private String subjectNm;
    private String description;
    private String language;
    private String level;
    private List<String> skills;

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getSubjectNm() { return subjectNm; }
    public void setSubjectNm(String subjectNm) { this.subjectNm = subjectNm; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}
