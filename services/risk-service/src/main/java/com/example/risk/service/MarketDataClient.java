package com.example.risk.service;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class MarketDataClient {

    private static final Logger log = LoggerFactory.getLogger(MarketDataClient.class);

    private final RestTemplate restTemplate;
    private final String fxUrl;

    public MarketDataClient(
            RestTemplateBuilder builder,
            @Value("${external.fx.url:https://api.exchangerate.host/latest?base=USD&symbols=MXN,EUR}") String fxUrl
    ) {
        this.fxUrl = fxUrl;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }

    public Map<String, Double> fetchLatestRates() {
        try {
            ResponseEntity<RateResponse> response = restTemplate.getForEntity(fxUrl, RateResponse.class);
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().rates() != null) {
                return response.getBody().rates();
            }
        } catch (RestClientException ex) {
            log.warn("No se pudieron obtener tasas externas: {}", ex.getMessage());
        }
        return Collections.emptyMap();
    }

    public record RateResponse(Map<String, Double> rates) {
    }
}
