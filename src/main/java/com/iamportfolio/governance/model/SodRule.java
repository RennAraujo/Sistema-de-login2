package com.iamportfolio.governance.model;

import com.iamportfolio.rbac.model.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sod_rules")
public class SodRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_a_id", nullable = false)
    private Role roleA;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_b_id", nullable = false)
    private Role roleB;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Role getRoleA() { return roleA; }
    public void setRoleA(Role roleA) { this.roleA = roleA; }
    public Role getRoleB() { return roleB; }
    public void setRoleB(Role roleB) { this.roleB = roleB; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
