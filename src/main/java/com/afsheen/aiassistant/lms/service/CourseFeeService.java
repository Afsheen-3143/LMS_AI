package com.afsheen.aiassistant.lms.service;

import com.afsheen.aiassistant.lms.dto.response.CourseFeeSettingResponse;

public interface CourseFeeService {

    CourseFeeSettingResponse getCourseFeeSetting(String courseId);
}
