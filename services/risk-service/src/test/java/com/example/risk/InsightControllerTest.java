package com.example.risk;

import com.example.risk.repository.RiskCaseRepository;
import com.example.risk.web.dto.BudgetSnapshotRequest;
import com.example.risk.web.dto.InsightsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InsightControllerTest {

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setupServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("external.fx.url", () -> mockWebServer.url("/latest").toString());
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RiskCaseRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void returnsInsightsWithBudgetAlertsAndRates() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"rates\":{\"MXN\":17.5}}")
                .addHeader("Content-Type", "application/json"));

        repository.deleteAll();
        InsightsRequest request = new InsightsRequest(
                "user-1",
                List.of(new BudgetSnapshotRequest("Alimentos", 400.0, 320.0, 0.8))
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/insights",
                request,
                String.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(json.get("insights").isArray()).isTrue();
        assertThat(json.get("insights").size()).isGreaterThanOrEqualTo(1);

        Map<String, Boolean> flags = Map.of(
                "hasRate", json.get("insights").toString().contains("MXN"),
                "hasBudgetAlert", json.get("insights").toString().contains("budget-Alimentos")
        );
        assertThat(flags.get("hasRate")).isTrue();
        assertThat(flags.get("hasBudgetAlert")).isTrue();

        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findAll().get(0).getReason()).contains("Alimentos");
    }
}
