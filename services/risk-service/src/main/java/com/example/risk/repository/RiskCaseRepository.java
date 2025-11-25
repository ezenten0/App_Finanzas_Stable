package com.example.risk.repository;

import com.example.risk.domain.RiskCase;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskCaseRepository extends JpaRepository<RiskCase, Long> {

    Optional<RiskCase> findTopByUserIdAndReason(String userId, String reason);
}
