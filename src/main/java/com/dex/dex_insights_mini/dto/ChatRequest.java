package com.dex.dex_insights_mini.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "Question cannot be blank")
    private String question;

    private String storeId;

    public String getStoreId() {
        return storeId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
}
