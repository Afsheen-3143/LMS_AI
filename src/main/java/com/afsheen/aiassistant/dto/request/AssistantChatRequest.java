package com.afsheen.aiassistant.dto.request;

public class AssistantChatRequest {

    private String message;

    // "sessionId" in API terms - same value, see ConversationIdSupport.
    // Leave blank to start a new session.
    private String conversationId;

    // Accepted for API-shape compatibility, but NOT trusted for
    // authorization: the student is always taken from the JWT
    // (see EnrollmentAssistantController.extractStudentIdFromJwt), so a
    // client cannot query another student's data by putting a different
    // studentId here.
    private String studentId;

    public AssistantChatRequest() {
    }

    public AssistantChatRequest(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
