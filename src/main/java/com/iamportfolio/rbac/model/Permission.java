package com.iamportfolio.rbac.model;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * A fine-grained capability check (e.g. "identity:write", "audit:read").
 * Roles aggregate permissions; users hold roles directly or via groups.
 * Permission name is what ends up in JWT scopes / Spring Security authorities.
 */
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 128)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    public Permission() {
    }

    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission p)) return false;
        return Objects.equals(name, p.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
