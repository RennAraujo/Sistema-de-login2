package com.iamportfolio.scim.outbound;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProvisioningEventRepository extends JpaRepository<ProvisioningEvent, Long> {
    List<ProvisioningEvent> findByUserIdOrderByAttemptedAtDesc(Long userId);
}
