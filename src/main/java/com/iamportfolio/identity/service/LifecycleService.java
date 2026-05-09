package com.iamportfolio.identity.service;

import com.iamportfolio.common.audit.Auditable;
import com.iamportfolio.identity.event.LifecycleEvent;
import com.iamportfolio.identity.model.LifecycleState;
import com.iamportfolio.identity.model.User;
import com.iamportfolio.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Joiner-Mover-Leaver state machine. Centralises every transition so a
 * caller cannot accidentally bypass an intermediate state, and emits a
 * LifecycleEvent for downstream listeners (audit, SCIM provisioning).
 */
@Service
public class LifecycleService {

    private static final Logger logger = LoggerFactory.getLogger(LifecycleService.class);

    /** Valid forward transitions; anything else throws IllegalStateException. */
    private static final Map<LifecycleState, Set<LifecycleState>> ALLOWED = new EnumMap<>(LifecycleState.class);

    static {
        ALLOWED.put(LifecycleState.PENDING_APPROVAL, EnumSet.of(LifecycleState.ACTIVE, LifecycleState.OFFBOARDED));
        ALLOWED.put(LifecycleState.ACTIVE,           EnumSet.of(LifecycleState.SUSPENDED, LifecycleState.OFFBOARDED));
        ALLOWED.put(LifecycleState.SUSPENDED,        EnumSet.of(LifecycleState.ACTIVE, LifecycleState.OFFBOARDED));
        ALLOWED.put(LifecycleState.OFFBOARDED,       EnumSet.of(LifecycleState.DELETED));
        ALLOWED.put(LifecycleState.DELETED,          EnumSet.noneOf(LifecycleState.class));
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    @Auditable(value = "LIFECYCLE_APPROVE", resourceType = "USER")
    public User approve(Long userId) {
        return transition(userId, LifecycleState.ACTIVE);
    }

    @Transactional
    @Auditable(value = "LIFECYCLE_SUSPEND", resourceType = "USER")
    public User suspend(Long userId) {
        return transition(userId, LifecycleState.SUSPENDED);
    }

    @Transactional
    @Auditable(value = "LIFECYCLE_REACTIVATE", resourceType = "USER")
    public User reactivate(Long userId) {
        return transition(userId, LifecycleState.ACTIVE);
    }

    @Transactional
    @Auditable(value = "LIFECYCLE_OFFBOARD", resourceType = "USER")
    public User offboard(Long userId) {
        User user = transition(userId, LifecycleState.OFFBOARDED);
        user.setTerminationDate(LocalDate.now());
        return userRepository.save(user);
    }

    private User transition(Long userId, LifecycleState target) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        LifecycleState current = user.getLifecycleState();
        if (current == target) {
            return user;
        }
        Set<LifecycleState> allowedFromCurrent = ALLOWED.getOrDefault(current, EnumSet.noneOf(LifecycleState.class));
        if (!allowedFromCurrent.contains(target)) {
            throw new IllegalStateException("Invalid transition: " + current + " -> " + target);
        }
        user.setLifecycleState(target);
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new LifecycleEvent(this, saved, current, target, currentActor()));
        logger.info("Lifecycle transition: user={} {} -> {}", saved.getUsername(), current, target);
        return saved;
    }

    private static String currentActor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth == null || !auth.isAuthenticated()) ? "system" : auth.getName();
    }
}
