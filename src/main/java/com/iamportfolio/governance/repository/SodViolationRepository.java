package com.iamportfolio.governance.repository;

import com.iamportfolio.governance.model.SodViolation;
import com.iamportfolio.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SodViolationRepository extends JpaRepository<SodViolation, Long> {
    Optional<SodViolation> findFirstBySodRuleIdAndUserAndResolvedAtIsNull(Long ruleId, User user);
    List<SodViolation> findByResolvedAtIsNull();
}
