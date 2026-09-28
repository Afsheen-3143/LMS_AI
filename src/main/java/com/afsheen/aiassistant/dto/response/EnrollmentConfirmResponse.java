package com.afsheen.aiassistant.dto.response;

public class EnrollmentConfirmResponse {

    private boolean success;
    private String message;
    private Long enrollmentId;

    public EnrollmentConfirmResponse() {
    }

    public EnrollmentConfirmResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
}
