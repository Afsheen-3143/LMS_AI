package com.afsheen.aiassistant.lms.dto.response;

public class CourseFeeSettingResponse {

    private String courseTitle;
    private String courseDuration;
    private CourseFeeHistoryResponse currentFee;

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getCourseDuration() { return courseDuration; }
    public void setCourseDuration(String courseDuration) { this.courseDuration = courseDuration; }

    public CourseFeeHistoryResponse getCurrentFee() { return currentFee; }
    public void setCurrentFee(CourseFeeHistoryResponse currentFee) { this.currentFee = currentFee; }
}
