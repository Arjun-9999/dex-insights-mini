package com.dex.dex_insights_mini.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChatResponse {
    private String answer;

    public List<Map<String, String>> getCitations() {
        return citations;
    }

    public void setCitations(List<Map<String, String>> citations) {
        this.citations = citations;
    }

    public String getRetrievedContextSummary() {
        return retrievedContextSummary;
    }

    public void setRetrievedContextSummary(String retrievedContextSummary) {
        this.retrievedContextSummary = retrievedContextSummary;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    private List<Map<String, String>> citations;
    private String retrievedContextSummary;

    public ChatResponse(String answer, List<Map<String, String>> citations, String summary) {
        this.answer = answer;
        this.citations = citations;
        this.retrievedContextSummary = summary;
    }
}