package com.afsheen.aiassistant.lms.dto.response;

public class ProgramFeeSettingResponse {

    private String programTitle;
    private String duration;
    private ProgramFeeHistoryResponse currentFee;

    public String getProgramTitle() { return programTitle; }
    public void setProgramTitle(String programTitle) { this.programTitle = programTitle; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public ProgramFeeHistoryResponse getCurrentFee() { return currentFee; }
    public void setCurrentFee(ProgramFeeHistoryResponse currentFee) { this.currentFee = currentFee; }
}
