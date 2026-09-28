package com.afsheen.aiassistant.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.afsheen.aiassistant.lms.entity.CourseFeeSetting;

@Repository
public interface CourseFeeSettingRepository extends JpaRepository<CourseFeeSetting, String> {
}
