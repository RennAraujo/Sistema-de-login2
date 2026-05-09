package com.iamportfolio.identity.model;

/**
 * Identity lifecycle states (Joiner-Mover-Leaver).
 * <p>
 * State machine (transitions enforced by LifecycleService):
 * <pre>
 *   PENDING_APPROVAL -> ACTIVE (approve)
 *   ACTIVE           -> SUSPENDED (suspend)
 *   SUSPENDED        -> ACTIVE (reactivate)
 *   ACTIVE           -> OFFBOARDED (offboard)
 *   SUSPENDED        -> OFFBOARDED (offboard)
 *   OFFBOARDED       -> DELETED (purge after retention period)
 * </pre>
 * Only ACTIVE allows authentication; everything else blocks login.
 */
public enum LifecycleState {
    PENDING_APPROVAL,
    ACTIVE,
    SUSPENDED,
    OFFBOARDED,
    DELETED
}
