package com.example.notification.web.dto;

import com.example.notification.domain.NotificationChannel;
import com.example.notification.domain.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotBlank(message = "recipient es requerido")
        @Size(max = 120)
        String recipient,
        @NotNull(message = "channel es requerido")
        NotificationChannel channel,
        @NotBlank(message = "subject es requerido")
        @Size(max = 140)
        String subject,
        @NotBlank(message = "body es requerido")
        @Size(max = 500)
        String body,
        @NotNull(message = "status es requerido")
        NotificationStatus status
) {
}
