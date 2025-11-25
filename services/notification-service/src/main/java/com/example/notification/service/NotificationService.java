package com.example.notification.service;

import com.example.notification.domain.Notification;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.web.dto.NotificationRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<Notification> findAll() {
        return repository.findAll();
    }

    public Optional<Notification> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Notification create(NotificationRequest request) {
        Notification notification = new Notification(
                null,
                request.recipient(),
                request.channel(),
                request.subject(),
                request.body(),
                request.status(),
                Instant.now()
        );
        return repository.save(notification);
    }

    @Transactional
    public Optional<Notification> update(Long id, NotificationRequest request) {
        return repository.findById(id).map(existing -> {
            existing.setRecipient(request.recipient());
            existing.setChannel(request.channel());
            existing.setSubject(request.subject());
            existing.setBody(request.body());
            existing.setStatus(request.status());
            return repository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
