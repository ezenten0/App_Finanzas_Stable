package com.example.risk;

import com.example.risk.domain.RiskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RiskCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("crea y devuelve un caso de riesgo")
    void shouldCreateAndGetRiskCase() throws Exception {
        String payload = objectMapper.writeValueAsString(new RiskCaseRequest(
                "USER-9",
                75,
                RiskStatus.OPEN,
                "Límite excedido"
        ));

        String location = mockMvc.perform(MockMvcRequestBuilders.post("/api/risk-cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(MockMvcRequestBuilders.get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("USER-9"))
                .andExpect(jsonPath("$.score").value(75));
    }

    private record RiskCaseRequest(String userId, Integer score, RiskStatus status, String reason) {
    }
}
