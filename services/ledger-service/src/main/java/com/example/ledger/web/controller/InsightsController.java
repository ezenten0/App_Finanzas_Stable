package com.example.ledger.web.controller;

import com.example.ledger.domain.InsightsSnapshot;
import com.example.ledger.service.InsightsService;
import com.example.ledger.web.dto.InsightsResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping
    public InsightsResponse getInsights(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        InsightsSnapshot snapshot = insightsService.getInsights(userId);
        return InsightsResponse.from(snapshot);
    }

    @PostMapping("/refresh")
    public InsightsResponse refreshInsights(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        InsightsSnapshot snapshot = insightsService.recalculate(userId);
        return InsightsResponse.from(snapshot);
    }
}
