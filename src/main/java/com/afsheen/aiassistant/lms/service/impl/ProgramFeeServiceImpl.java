package com.afsheen.aiassistant.lms.service.impl;

import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.exceptions.ResourceNotFoundException;
import com.afsheen.aiassistant.lms.dto.response.ProgramFeeHistoryResponse;
import com.afsheen.aiassistant.lms.dto.response.ProgramFeeSettingResponse;
import com.afsheen.aiassistant.lms.entity.ProgramFeeSetting;
import com.afsheen.aiassistant.lms.repository.ProgramFeeSettingRepository;
import com.afsheen.aiassistant.lms.repository.ProgramRepository;
import com.afsheen.aiassistant.lms.service.ProgramFeeService;

@Service
public class ProgramFeeServiceImpl implements ProgramFeeService {

    private final ProgramFeeSettingRepository programFeeSettingRepository;
    private final ProgramRepository programRepository;

    public ProgramFeeServiceImpl(ProgramFeeSettingRepository programFeeSettingRepository, ProgramRepository programRepository) {
        this.programFeeSettingRepository = programFeeSettingRepository;
        this.programRepository = programRepository;
    }

    @Override
    public ProgramFeeSettingResponse getProgramFeeSetting(String programId) {
        ProgramFeeSetting setting = programFeeSettingRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("No fee configured for program: " + programId));

        ProgramFeeHistoryResponse history = new ProgramFeeHistoryResponse();
        history.setFee(setting.getFee());
        history.setDiscount(setting.getDiscount());

        ProgramFeeSettingResponse response = new ProgramFeeSettingResponse();
        response.setProgramTitle(programRepository.findById(programId)
                .map(p -> p.getProgramTitle())
                .orElse(null));
        response.setDuration(setting.getDuration());
        response.setCurrentFee(history);
        return response;
    }
}
