package com.example.notification;

import com.example.notification.domain.NotificationChannel;
import com.example.notification.domain.NotificationStatus;
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
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("crea y consulta una notificación")
    void shouldCreateAndFetchNotification() throws Exception {
        String payload = objectMapper.writeValueAsString(new NotificationRequest(
                "user@example.com",
                NotificationChannel.EMAIL,
                "Recordatorio",
                "Tienes un pago pendiente",
                NotificationStatus.PENDING
        ));

        String location = mockMvc.perform(MockMvcRequestBuilders.post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(MockMvcRequestBuilders.get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipient").value("user@example.com"))
                .andExpect(jsonPath("$.channel").value("EMAIL"));
    }

    private record NotificationRequest(String recipient,
                                       NotificationChannel channel,
                                       String subject,
                                       String body,
                                       NotificationStatus status) {
    }
}
