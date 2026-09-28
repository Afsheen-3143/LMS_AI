package com.afsheen.aiassistant.lms.service;

import com.afsheen.aiassistant.lms.dto.request.AssignProgramRequest;
import com.afsheen.aiassistant.lms.dto.response.AssignProgramResponse;

public interface StudentProgramService {

    AssignProgramResponse assignProgramToStudent(AssignProgramRequest request);
}
