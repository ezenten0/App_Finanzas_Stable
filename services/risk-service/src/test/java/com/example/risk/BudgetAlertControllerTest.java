package com.example.risk;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.example.risk.repository.RiskCaseRepository;
import com.example.risk.web.dto.BudgetAlertWebhookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
        try (MockedStatic<FirebaseAuth> firebaseAuth = mockStatic(FirebaseAuth.class)) {
            FirebaseAuth auth = mock(FirebaseAuth.class);
            FirebaseToken token = mock(FirebaseToken.class);
            when(token.getUid()).thenReturn("test-user");
            firebaseAuth.when(FirebaseAuth::getInstance).thenReturn(auth);
            when(auth.verifyIdToken("valid-token")).thenReturn(token);

        BudgetAlertWebhookRequest request = new BudgetAlertWebhookRequest(
                "user-1",
                "Alimentos",
                400.0,
                320.0,
                0.8,
                0.75
        );

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("valid-token");
            HttpEntity<BudgetAlertWebhookRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> first = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/budget-alerts",
                    entity,
                String.class
        );

            ResponseEntity<String> second = restTemplate.postForEntity(
                    "http://localhost:" + port + "/api/v1/budget-alerts",
                    entity,
                    String.class
            );

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(riskCaseRepository.findAll()).hasSize(1);
        assertThat(riskCaseRepository.findAll().get(0).getScore()).isEqualTo(80);
        }
    }
}
