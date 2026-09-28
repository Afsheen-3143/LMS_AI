package com.afsheen.aiassistant.dto.response;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private String role;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessageResponse() {
    }

    public ChatMessageResponse(String role, String content, LocalDateTime timestamp) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
