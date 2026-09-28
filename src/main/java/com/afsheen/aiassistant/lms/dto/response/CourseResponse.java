package com.afsheen.aiassistant.lms.dto.response;

import java.util.List;

/** Fictional demo shape - fields mirror what AssistantTools/DocumentIngestionService read. */
public class CourseResponse {

    private String courseId;
    private String courseTitle;
    private String description;
    private String language;
    private List<String> skills;
    private String subjectNm;
    private String level;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getSubjectNm() { return subjectNm; }
    public void setSubjectNm(String subjectNm) { this.subjectNm = subjectNm; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
