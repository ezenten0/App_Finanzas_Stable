package com.example.notification.web.controller;

import com.example.notification.domain.Notification;
import com.example.notification.service.NotificationService;
import com.example.notification.web.dto.NotificationRequest;
import com.example.notification.web.dto.NotificationResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> findAll() {
        return service.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(notification -> ResponseEntity.ok(toResponse(notification)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {
        Notification created = service.create(request);
        return ResponseEntity.created(URI.create("/api/notifications/" + created.getId()))
                .body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody NotificationRequest request) {
        return service.update(id, request)
                .map(updated -> ResponseEntity.ok(toResponse(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipient(),
                notification.getChannel(),
                notification.getSubject(),
                notification.getBody(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}
