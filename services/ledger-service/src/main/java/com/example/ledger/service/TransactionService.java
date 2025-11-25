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
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionEventPublisher eventPublisher;

    public TransactionService(TransactionRepository repository, TransactionEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }

    public Optional<Transaction> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Transaction create(TransactionRequest request) {
        Transaction transaction = new Transaction(
                null,
                "mobile-account",
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
        Transaction saved = repository.save(transaction);
        eventPublisher.publishTransactionSignal("transaction-created");
        return saved;
    }

    @Transactional
    public Optional<Transaction> update(Long id, TransactionRequest request) {
        return repository.findById(id).map(existing -> {
            existing.setType(request.type());
            existing.setAmount(request.amount());
            existing.setTitle(request.title());
            existing.setDescription(request.description());
            existing.setCategory(request.category());
            existing.setEventDate(LocalDate.parse(request.date()));
            Transaction updated = repository.save(existing);
            eventPublisher.publishTransactionSignal("transaction-updated");
            return updated;
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            eventPublisher.publishTransactionSignal("transaction-deleted");
            return true;
        }
        return false;
    }
}
