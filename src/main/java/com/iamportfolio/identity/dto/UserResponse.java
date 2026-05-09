package com.iamportfolio.identity.dto;

import com.iamportfolio.identity.model.LifecycleState;
import com.iamportfolio.identity.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        LifecycleState lifecycleState,
        String department,
        String externalId,
        LocalDate hireDate,
        LocalDate terminationDate,
        boolean twoFactorEnabled,
        LocalDateTime createdAt,
        LocalDateTime lastLogin
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(), u.getUsername(), u.getEmail(),
                u.getFirstName(), u.getLastName(),
                u.getLifecycleState(), u.getDepartment(), u.getExternalId(),
                u.getHireDate(), u.getTerminationDate(),
                u.isTwoFactorEnabled(), u.getCreatedAt(), u.getLastLogin()
        );
    }
}
