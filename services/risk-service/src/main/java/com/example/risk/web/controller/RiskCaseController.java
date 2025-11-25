package com.example.risk.web.controller;

import com.example.risk.domain.RiskCase;
import com.example.risk.service.RiskCaseService;
import com.example.risk.web.dto.RiskCaseRequest;
import com.example.risk.web.dto.RiskCaseResponse;
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
@RequestMapping("/api/risk-cases")
public class RiskCaseController {

    private final RiskCaseService service;

    public RiskCaseController(RiskCaseService service) {
        this.service = service;
    }

    @GetMapping
    public List<RiskCaseResponse> findAll() {
        return service.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RiskCaseResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(caseItem -> ResponseEntity.ok(toResponse(caseItem)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RiskCaseResponse> create(@Valid @RequestBody RiskCaseRequest request) {
        RiskCase created = service.create(request);
        return ResponseEntity.created(URI.create("/api/risk-cases/" + created.getId()))
                .body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RiskCaseResponse> update(@PathVariable Long id, @Valid @RequestBody RiskCaseRequest request) {
        return service.update(id, request)
                .map(updated -> ResponseEntity.ok(toResponse(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private RiskCaseResponse toResponse(RiskCase riskCase) {
        return new RiskCaseResponse(
                riskCase.getId(),
                riskCase.getUserId(),
                riskCase.getScore(),
                riskCase.getStatus(),
                riskCase.getReason(),
                riskCase.getCreatedAt()
        );
    }
}
