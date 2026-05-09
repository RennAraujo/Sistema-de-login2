package com.iamportfolio.governance.controller;

import com.iamportfolio.common.audit.Auditable;
import com.iamportfolio.governance.model.SodRule;
import com.iamportfolio.governance.model.SodViolation;
import com.iamportfolio.governance.repository.SodRuleRepository;
import com.iamportfolio.governance.repository.SodViolationRepository;
import com.iamportfolio.governance.service.SodCheckJob;
import com.iamportfolio.rbac.model.Role;
import com.iamportfolio.rbac.repository.RoleRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Read + manage SoD rules and inspect open violations.
 * Gated by governance:manage / ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/governance")
public class SodController {

    @Autowired private SodRuleRepository ruleRepository;
    @Autowired private SodViolationRepository violationRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SodCheckJob sodCheckJob;

    @GetMapping("/sod-rules")
    public List<Map<String, Object>> listRules() {
        return ruleRepository.findAll().stream().map(SodController::toMap).toList();
    }

    public record CreateSodRule(@NotBlank String name, @NotBlank String roleA,
                                @NotBlank String roleB, String description) {}

    @PostMapping("/sod-rules")
    @Auditable(value = "SOD_RULE_CREATE", resourceType = "SOD_RULE")
    public Map<String, Object> create(@RequestBody CreateSodRule req) {
        Role a = roleRepository.findByName(req.roleA())
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + req.roleA()));
        Role b = roleRepository.findByName(req.roleB())
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + req.roleB()));
        if (a.getName().equals(b.getName())) {
            throw new IllegalArgumentException("roleA and roleB must differ");
        }
        SodRule rule = new SodRule();
        rule.setName(req.name());
        rule.setRoleA(a);
        rule.setRoleB(b);
        rule.setDescription(req.description());
        return toMap(ruleRepository.save(rule));
    }

    @GetMapping("/sod-violations")
    public List<Map<String, Object>> openViolations() {
        return violationRepository.findByResolvedAtIsNull().stream()
                .map(v -> Map.<String, Object>of(
                        "id", v.getId(),
                        "rule", v.getSodRule().getName(),
                        "user", v.getUser().getUsername(),
                        "detectedAt", v.getDetectedAt()
                )).toList();
    }

    @PostMapping("/sod-violations/{id}/resolve")
    @Auditable(value = "SOD_VIOLATION_RESOLVE", resourceType = "SOD_VIOLATION")
    public Map<String, Object> resolve(@PathVariable Long id) {
        SodViolation v = violationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Violation not found: " + id));
        v.setResolvedAt(LocalDateTime.now());
        violationRepository.save(v);
        return Map.of("id", v.getId(), "resolvedAt", v.getResolvedAt());
    }

    @PostMapping("/sod-check/run")
    @Auditable(value = "SOD_CHECK_MANUAL", resourceType = "SOD")
    public Map<String, String> runNow() {
        sodCheckJob.run();
        return Map.of("status", "completed");
    }

    private static Map<String, Object> toMap(SodRule r) {
        return Map.of(
                "id", r.getId(),
                "name", r.getName(),
                "roleA", r.getRoleA().getName(),
                "roleB", r.getRoleB().getName(),
                "description", r.getDescription() == null ? "" : r.getDescription(),
                "enabled", r.isEnabled()
        );
    }
}
