package com.afsheen.aiassistant.lms.service;

import com.afsheen.aiassistant.lms.dto.response.ProgramFeeSettingResponse;

public interface ProgramFeeService {

    ProgramFeeSettingResponse getProgramFeeSetting(String programId);
}
