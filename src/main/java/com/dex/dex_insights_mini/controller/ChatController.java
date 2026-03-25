package com.dex.dex_insights_mini.controller;


import com.dex.dex_insights_mini.dto.ChatRequest;
import com.dex.dex_insights_mini.dto.ChatResponse;
import com.dex.dex_insights_mini.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        log.info("event=chat_request storeId={} questionLength={}",
                request.getStoreId(),
                request.getQuestion() == null ? 0 : request.getQuestion().length());
        return service.answerQuestion(request);
    }
}
