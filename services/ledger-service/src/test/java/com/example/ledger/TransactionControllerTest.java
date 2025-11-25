package com.example.ledger;

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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("expone un stream SSE para actualizaciones en tiempo real")
    void shouldExposeSseEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(header -> header.getResponse().getContentType().contains("text/event-stream"));
    }

    @Test
    @DisplayName("crea y consulta una transacción")
    void shouldCreateAndFetchTransaction() throws Exception {
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
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(MockMvcRequestBuilders.get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pago recibido"))
                .andExpect(jsonPath("$.amount").value(120.50));
    }
}
