package com.iamportfolio.rbac.model;

import com.iamportfolio.identity.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Direct user-role grant with audit metadata: who assigned it, when, and
 * (for time-boxed access) when it expires. Group-mediated roles do not
 * use this entity — they live on Group.roles.
 */
@Entity
@Table(
        name = "role_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uk_role_assignment", columnNames = {"user_id", "role_id"})
)
public class RoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public RoleAssignment() {
    }

    public RoleAssignment(User user, Role role, String assignedBy, LocalDateTime expiresAt) {
        this.user = user;
        this.role = role;
        this.assignedBy = assignedBy;
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
