package com.afsheen.aiassistant.lms.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_fee_setting")
public class CourseFeeSetting {

    /** Same value as the owning course's id - one current fee setting per course. */
    @Id
    @Column(name = "course_id", length = 10)
    private String courseId;

    @Column(name = "course_duration", length = 50)
    private String courseDuration;

    @Column(name = "fee", precision = 12, scale = 2)
    private BigDecimal fee;

    @Column(name = "discount", precision = 12, scale = 2)
    private BigDecimal discount;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseDuration() { return courseDuration; }
    public void setCourseDuration(String courseDuration) { this.courseDuration = courseDuration; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
}
