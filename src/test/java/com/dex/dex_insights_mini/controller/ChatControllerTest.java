package com.dex.dex_insights_mini.controller;

import com.dex.dex_insights_mini.dto.ChatResponse;
import com.dex.dex_insights_mini.exception.StoreNotFoundException;
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
import static org.mockito.ArgumentMatchers.argThat;
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("question: Question cannot be blank"))
                .andExpect(jsonPath("$.path").value("/v1/chat"));

        verify(chatService, never()).answerQuestion(any());
    }

    @Test
    void chat_returnsBadRequestWhenJsonMalformed() throws Exception {
        mockMvc.perform(post("/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"hello\",\"storeId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"))
                .andExpect(jsonPath("$.path").value("/v1/chat"));

        verify(chatService, never()).answerQuestion(any());
    }

    @Test
    void chat_returnsNotFoundWhenStoreIdUnrecognized() throws Exception {
        when(chatService.answerQuestion(any())).thenThrow(new StoreNotFoundException("99999"));

        mockMvc.perform(post("/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "show incidents for store 99999"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Store not found: 99999"))
                .andExpect(jsonPath("$.path").value("/v1/chat"));
    }

    @Test
    void chat_forwardsSelectedStoreContextWhenQuestionContainsBareStoreNumber() throws Exception {
        ChatResponse response = new ChatResponse(
                "Store 10001 (7-Eleven, Austin) has 1 offline pump.",
                List.of(Map.of("type", "store", "storeId", "10001")),
                "Evidence from selected store context"
        );
        when(chatService.answerQuestion(any())).thenReturn(response);

        mockMvc.perform(post("/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "10002 offline",
                                  "storeId": "10001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Store 10001 (7-Eleven, Austin) has 1 offline pump."))
                .andExpect(jsonPath("$.citations[0].storeId").value("10001"));

        verify(chatService).answerQuestion(argThat(req ->
                "10001".equals(req.getStoreId()) && "10002 offline".equals(req.getQuestion())));
    }
}

