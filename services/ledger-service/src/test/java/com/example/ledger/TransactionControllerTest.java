package com.example.ledger;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.example.ledger.domain.TransactionType;
import com.example.ledger.web.dto.TransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class TransactionControllerTest {

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
    @DisplayName("expone un stream SSE para actualizaciones en tiempo real")
    void shouldExposeSseEndpoint() throws Exception {
        mockFirebase();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions/stream")
                        .header("Authorization", "Bearer valid-token")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(header -> header.getResponse().getContentType().contains("text/event-stream"));
        closeFirebaseMock();
    }

    @Test
    @DisplayName("crea y consulta una transacción")
    void shouldCreateAndFetchTransaction() throws Exception {
        mockFirebase();
        String payload = objectMapper.writeValueAsString(new TransactionRequest(
                "Pago recibido",
                TransactionType.CREDIT,
                BigDecimal.valueOf(120.50),
                "Pago mensual",
                "Ingresos",
                LocalDate.now().toString()
        ));

        String location = mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions")
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
                .andExpect(jsonPath("$.title").value("Pago recibido"))
                .andExpect(jsonPath("$.amount").value(120.50));
        closeFirebaseMock();
    }
}
