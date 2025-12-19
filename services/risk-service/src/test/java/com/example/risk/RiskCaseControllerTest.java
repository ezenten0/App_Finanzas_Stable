package com.example.risk;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
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
import org.mockito.MockedStatic;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class RiskCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockedStatic<FirebaseAuth> firebaseAuth;

    private void mockFirebase() throws Exception {
        firebaseAuth = mockStatic(FirebaseAuth.class);
        FirebaseAuth auth = mock(FirebaseAuth.class);
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("test-user");
        firebaseAuth.when(FirebaseAuth::getInstance).thenReturn(auth);
        when(auth.verifyIdToken("valid-token")).thenReturn(token);
    }

    private void closeFirebaseMock() {
        if (firebaseAuth != null) {
            firebaseAuth.close();
        }
    }

    @Test
    @DisplayName("crea y devuelve un caso de riesgo")
    void shouldCreateAndGetRiskCase() throws Exception {
        mockFirebase();
        String payload = objectMapper.writeValueAsString(new RiskCaseRequest(
                "USER-9",
                75,
                RiskStatus.OPEN,
                "Límite excedido"
        ));

        String location = mockMvc.perform(MockMvcRequestBuilders.post("/api/risk-cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(MockMvcRequestBuilders.get(location)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("test-user"))
                .andExpect(jsonPath("$.score").value(75));
        closeFirebaseMock();
    }

    private record RiskCaseRequest(String userId, Integer score, RiskStatus status, String reason) {
    }
}
