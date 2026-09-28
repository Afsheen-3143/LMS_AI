package com.afsheen.aiassistant.lms.service.impl;

import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.lms.dto.request.AssignProgramRequest;
import com.afsheen.aiassistant.lms.dto.response.AssignProgramResponse;
import com.afsheen.aiassistant.lms.service.StudentProgramService;

/** Demo stand-in - see StudentCourseServiceImpl for why this just no-ops. */
@Service
public class StudentProgramServiceImpl implements StudentProgramService {

    @Override
    public AssignProgramResponse assignProgramToStudent(AssignProgramRequest request) {
        return new AssignProgramResponse();
    }
}
