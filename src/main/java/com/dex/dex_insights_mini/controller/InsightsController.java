package com.dex.dex_insights_mini.controller;


import com.dex.dex_insights_mini.service.InsightsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/insights")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class InsightsController {
    private static final Logger log = LoggerFactory.getLogger(InsightsController.class);
    private final InsightsService service;

    public InsightsController(InsightsService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        log.info("event=get_insights_overview");
        return service.generateOverview();
    }
}

