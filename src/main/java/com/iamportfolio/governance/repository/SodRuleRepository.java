package com.iamportfolio.governance.repository;

import com.iamportfolio.governance.model.SodRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SodRuleRepository extends JpaRepository<SodRule, Long> {
    List<SodRule> findByEnabledTrue();
}
