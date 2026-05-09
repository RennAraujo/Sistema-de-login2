package com.iamportfolio.rbac.repository;

import com.iamportfolio.identity.model.User;
import com.iamportfolio.rbac.model.Role;
import com.iamportfolio.rbac.model.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {

    List<RoleAssignment> findByUser(User user);

    Optional<RoleAssignment> findByUserAndRole(User user, Role role);

    @Query("SELECT ra FROM RoleAssignment ra WHERE ra.user = :user " +
           "AND (ra.expiresAt IS NULL OR ra.expiresAt > :now)")
    List<RoleAssignment> findActiveByUser(User user, LocalDateTime now);
}
