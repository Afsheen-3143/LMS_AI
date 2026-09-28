package com.afsheen.aiassistant.lms.entity;

import com.afsheen.aiassistant.entity.base.AuditFields;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course")
public class Course extends AuditFields {

    @Id
    @Column(name = "course_id", length = 10)
    private String courseId;

    @Column(name = "course_title", nullable = false, length = 200)
    private String courseTitle;

    @Column(name = "subject_nm", length = 100)
    private String subjectNm;

    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "level", length = 30)
    private String level;

    /** Comma-separated skill tags - mapped to/from a List&lt;String&gt; in the service layer. */
    @Column(name = "skills", length = 500)
    private String skills;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

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

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
}
