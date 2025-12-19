package com.example.ledger.service;

import com.example.ledger.domain.Transaction;
import com.example.ledger.domain.TransactionStatus;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.web.dto.TransactionRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final InsightsService insightsService;

    public TransactionService(TransactionRepository repository, InsightsService insightsService) {
        this.repository = repository;
        this.insightsService = insightsService;
    }

    public List<Transaction> findAll(String userId) {
        return repository.findAllForUser(userId);
    }

    public Optional<Transaction> findById(String userId, String id) {
        return repository.findById(userId, id);
    }

    public Transaction create(String userId, TransactionRequest request) {
        Transaction transaction = new Transaction(
                null,
                userId,
                request.type(),
                request.amount(),
                request.title(),
                request.description(),
                request.category(),
                LocalDate.parse(request.date()),
                TransactionStatus.POSTED,
                "USD",
                Instant.now()
        );
        Transaction created = repository.save(userId, transaction);
        insightsService.recalculate(userId);
        return created;
    }

    public Optional<Transaction> update(String userId, String id, TransactionRequest request) {
        return repository.findById(userId, id).map(existing -> {
            existing.setType(request.type());
            existing.setAmount(request.amount());
            existing.setTitle(request.title());
            existing.setDescription(request.description());
            existing.setCategory(request.category());
            existing.setEventDate(LocalDate.parse(request.date()));
            Transaction updated = repository.save(userId, existing);
            insightsService.recalculate(userId);
            return updated;
        });
    }

    public boolean delete(String userId, String id) {
        boolean deleted = repository.delete(userId, id);
        if (deleted) {
            insightsService.recalculate(userId);
        }
        return deleted;
    }
}
