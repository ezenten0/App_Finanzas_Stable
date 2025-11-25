package com.example.ledger.web.controller;

import com.example.ledger.domain.Transaction;
import com.example.ledger.service.TransactionService;
import com.example.ledger.service.TransactionEventPublisher;
import com.example.ledger.web.dto.TransactionRequest;
import com.example.ledger.web.dto.TransactionResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;
    private final TransactionEventPublisher eventPublisher;

    public TransactionController(TransactionService service, TransactionEventPublisher eventPublisher) {
        this.service = service;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return eventPublisher.register();
    }

    @GetMapping
    public List<TransactionResponse> findAll() {
        return service.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(transaction -> ResponseEntity.ok(toResponse(transaction)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        Transaction created = service.create(request);
        return ResponseEntity.created(URI.create("/api/transactions/" + created.getId()))
                .body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody TransactionRequest request) {
        return service.update(id, request)
                .map(updated -> ResponseEntity.ok(toResponse(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTitle(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getEventDate(),
                transaction.getCreatedAt()
        );
    }
}
