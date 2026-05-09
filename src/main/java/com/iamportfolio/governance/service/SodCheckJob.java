package com.iamportfolio.governance.service;

import com.iamportfolio.common.audit.AuditService;
import com.iamportfolio.governance.model.SodRule;
import com.iamportfolio.governance.model.SodViolation;
import com.iamportfolio.governance.repository.SodRuleRepository;
import com.iamportfolio.governance.repository.SodViolationRepository;
import com.iamportfolio.identity.model.User;
import com.iamportfolio.identity.repository.UserRepository;
import com.iamportfolio.rbac.model.RoleAssignment;
import com.iamportfolio.rbac.repository.RoleAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Scans every enabled SoD rule against current role assignments and
 * records any violations (no duplicates: one open SodViolation per
 * (rule, user) pair). Runs every 30 minutes; can be triggered manually
 * via the controller for ad-hoc checks.
 */
@Component
public class SodCheckJob {

    private static final Logger logger = LoggerFactory.getLogger(SodCheckJob.class);

    @Autowired private SodRuleRepository ruleRepository;
    @Autowired private SodViolationRepository violationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleAssignmentRepository roleAssignmentRepository;
    @Autowired private AuditService auditService;

    @Scheduled(fixedDelayString = "${app.governance.sod-check-interval-ms:1800000}",
               initialDelayString = "${app.governance.sod-check-initial-delay-ms:60000}")
    @Transactional
    public void run() {
        List<SodRule> rules = ruleRepository.findByEnabledTrue();
        if (rules.isEmpty()) return;

        int detected = 0;
        for (User user : userRepository.findAll()) {
            Set<String> roleNames = new HashSet<>();
            for (RoleAssignment ra : roleAssignmentRepository.findActiveByUser(user, LocalDateTime.now())) {
                roleNames.add(ra.getRole().getName());
            }
            user.getGroups().forEach(g -> g.getRoles().forEach(r -> roleNames.add(r.getName())));

            for (SodRule rule : rules) {
                if (roleNames.contains(rule.getRoleA().getName())
                        && roleNames.contains(rule.getRoleB().getName())) {
                    boolean alreadyOpen = violationRepository
                            .findFirstBySodRuleIdAndUserAndResolvedAtIsNull(rule.getId(), user)
                            .isPresent();
                    if (!alreadyOpen) {
                        SodViolation v = new SodViolation();
                        v.setSodRule(rule);
                        v.setUser(user);
                        violationRepository.save(v);
                        detected++;
                        auditService.record("SOD_VIOLATION_DETECTED", "USER",
                                String.valueOf(user.getId()), "SUCCESS",
                                Map.of("rule", rule.getName(),
                                       "user", user.getUsername()));
                    }
                }
            }
        }
        if (detected > 0) {
            logger.warn("SodCheckJob detected {} new SoD violation(s)", detected);
        } else {
            logger.debug("SodCheckJob ran clean — no new violations.");
        }
    }
}
