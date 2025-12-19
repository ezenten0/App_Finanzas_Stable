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
@RequestMapping("/api/risk-cases")
public class RiskCaseController {

    private final RiskCaseService service;

    public RiskCaseController(RiskCaseService service) {
        this.service = service;
    }

    @GetMapping
    public List<RiskCaseResponse> findAll(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return service.findAll(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RiskCaseResponse> findById(@PathVariable Long id, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return service.findById(id, userId)
                .map(caseItem -> ResponseEntity.ok(toResponse(caseItem)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RiskCaseResponse> create(@Valid @RequestBody RiskCaseRequest request,
                                                   Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        RiskCase created = service.create(request.withUser(userId));
        return ResponseEntity.created(URI.create("/api/risk-cases/" + created.getId()))
                .body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RiskCaseResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody RiskCaseRequest request,
                                                   Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return service.update(id, request.withUser(userId))
                .map(updated -> ResponseEntity.ok(toResponse(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return service.delete(id, userId) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
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
