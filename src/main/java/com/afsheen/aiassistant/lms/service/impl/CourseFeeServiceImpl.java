package com.afsheen.aiassistant.lms.service.impl;

import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.exceptions.ResourceNotFoundException;
import com.afsheen.aiassistant.lms.dto.response.CourseFeeHistoryResponse;
import com.afsheen.aiassistant.lms.dto.response.CourseFeeSettingResponse;
import com.afsheen.aiassistant.lms.entity.CourseFeeSetting;
import com.afsheen.aiassistant.lms.repository.CourseFeeSettingRepository;
import com.afsheen.aiassistant.lms.repository.CourseRepository;
import com.afsheen.aiassistant.lms.service.CourseFeeService;

@Service
public class CourseFeeServiceImpl implements CourseFeeService {

    private final CourseFeeSettingRepository courseFeeSettingRepository;
    private final CourseRepository courseRepository;

    public CourseFeeServiceImpl(CourseFeeSettingRepository courseFeeSettingRepository, CourseRepository courseRepository) {
        this.courseFeeSettingRepository = courseFeeSettingRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public CourseFeeSettingResponse getCourseFeeSetting(String courseId) {
        CourseFeeSetting setting = courseFeeSettingRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("No fee configured for course: " + courseId));

        CourseFeeHistoryResponse history = new CourseFeeHistoryResponse();
        history.setFee(setting.getFee());
        history.setDiscount(setting.getDiscount());

        CourseFeeSettingResponse response = new CourseFeeSettingResponse();
        response.setCourseTitle(courseRepository.findById(courseId)
                .map(c -> c.getCourseTitle())
                .orElse(null));
        response.setCourseDuration(setting.getCourseDuration());
        response.setCurrentFee(history);
        return response;
    }
}
