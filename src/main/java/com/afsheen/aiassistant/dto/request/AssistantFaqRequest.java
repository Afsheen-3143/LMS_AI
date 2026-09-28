package com.afsheen.aiassistant.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AssistantFaqRequest {

    @NotBlank
    private String category;

    @NotBlank
    private String question;

    @NotBlank
    private String answer;

    private boolean active = true;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
