package com.example.risk;

import com.example.risk.repository.RiskCaseRepository;
import com.example.risk.web.dto.BudgetAlertWebhookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BudgetAlertControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RiskCaseRepository riskCaseRepository;

    @BeforeEach
    void cleanRepository() {
        riskCaseRepository.deleteAll();
    }

    @Test
    void storesBudgetAlertIdempotently() {
        BudgetAlertWebhookRequest request = new BudgetAlertWebhookRequest(
                "user-1",
                "Alimentos",
                400.0,
                320.0,
                0.8,
                0.75
        );

        ResponseEntity<String> first = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/budget-alerts",
                request,
                String.class
        );
        ResponseEntity<String> second = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/budget-alerts",
                request,
                String.class
        );

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(riskCaseRepository.findAll()).hasSize(1);
        assertThat(riskCaseRepository.findAll().get(0).getScore()).isEqualTo(80);
    }
}
