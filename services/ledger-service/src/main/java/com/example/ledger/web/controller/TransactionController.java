package com.example.ledger.web.controller;

import com.example.ledger.domain.Transaction;
import com.example.ledger.service.TransactionService;
import com.example.ledger.web.dto.TransactionRequest;
import com.example.ledger.web.dto.TransactionResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping
    public List<TransactionResponse> findAll(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return service.findAll(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable String id, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return service.findById(userId, id)
                .map(transaction -> ResponseEntity.ok(toResponse(transaction)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request,
                                                      Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        Transaction created = service.create(userId, request);
        return ResponseEntity.created(URI.create("/api/transactions/" + created.getId()))
                .body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable String id,
                                                      @Valid @RequestBody TransactionRequest request,
                                                      Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return service.update(userId, id, request)
                .map(updated -> ResponseEntity.ok(toResponse(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return service.delete(userId, id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
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
