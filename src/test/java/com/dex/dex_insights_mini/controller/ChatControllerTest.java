package com.dex.dex_insights_mini.controller;

import com.dex.dex_insights_mini.dto.ChatResponse;
import com.dex.dex_insights_mini.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Test
    void chat_returnsResponseFromService() throws Exception {
        ChatResponse response = new ChatResponse(
                "answer",
                List.of(Map.of("type", "store", "storeId", "S1")),
                "summary"
        );

        when(chatService.answerQuestion(any())).thenReturn(response);

        mockMvc.perform(post("/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "How is store S1?",
                                  "storeId": "S1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("answer"))
                .andExpect(jsonPath("$.retrievedContextSummary").value("summary"))
                .andExpect(jsonPath("$.citations[0].type").value("store"))
                .andExpect(jsonPath("$.citations[0].storeId").value("S1"));
    }

    @Test
    void chat_returnsBadRequestWhenQuestionBlank() throws Exception {
        mockMvc.perform(post("/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "",
                                  "storeId": "S1"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).answerQuestion(any());
    }
}

