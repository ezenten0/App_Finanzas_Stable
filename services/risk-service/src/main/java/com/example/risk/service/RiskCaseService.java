package com.example.risk.service;

import com.example.risk.domain.RiskCase;
import com.example.risk.repository.RiskCaseRepository;
import com.example.risk.web.dto.RiskCaseRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskCaseService {

    private final RiskCaseRepository repository;

    public RiskCaseService(RiskCaseRepository repository) {
        this.repository = repository;
    }

    public List<RiskCase> findAll(String userId) {
        return repository.findAllByUserId(userId);
    }

    public Optional<RiskCase> findById(Long id, String userId) {
        return repository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public RiskCase createOrUpdate(RiskCaseRequest request) {
        return repository.findTopByUserIdAndReason(request.userId(), request.reason())
                .map(existing -> {
                    existing.setScore(request.score());
                    existing.setStatus(request.status());
                    existing.setReason(request.reason());
                    return repository.save(existing);
                })
                .orElseGet(() -> create(request));
    }

    @Transactional
    public RiskCase create(RiskCaseRequest request) {
        RiskCase riskCase = new RiskCase(
                null,
                request.userId(),
                request.score(),
                request.status(),
                request.reason(),
                Instant.now()
        );
        return repository.save(riskCase);
    }

    @Transactional
    public Optional<RiskCase> update(Long id, RiskCaseRequest request) {
        return repository.findByIdAndUserId(id, request.userId()).map(existing -> {
            existing.setUserId(request.userId());
            existing.setScore(request.score());
            existing.setStatus(request.status());
            existing.setReason(request.reason());
            return repository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id, String userId) {
        if (repository.findByIdAndUserId(id, userId).isPresent()) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
