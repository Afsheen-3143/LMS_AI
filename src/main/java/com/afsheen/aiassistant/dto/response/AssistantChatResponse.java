package com.afsheen.aiassistant.dto.response;

import java.util.Map;

public class AssistantChatResponse {

    private String response;
    private String conversationId;
    private String actionRequired;
    private Map<String, Object> pendingAction;

    public AssistantChatResponse() {
    }

    public AssistantChatResponse(String response, String conversationId) {
        this.response = response;
        this.conversationId = conversationId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }

    public Map<String, Object> getPendingAction() {
        return pendingAction;
    }

    public void setPendingAction(Map<String, Object> pendingAction) {
        this.pendingAction = pendingAction;
    }
}
