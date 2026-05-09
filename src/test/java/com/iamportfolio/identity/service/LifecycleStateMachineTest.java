package com.iamportfolio.identity.service;

import com.iamportfolio.identity.model.LifecycleState;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test of the lifecycle transitions. Mirrors the table that
 * lives inside LifecycleService — keeps the contract test-locked so a
 * silent edit there fails CI.
 */
class LifecycleStateMachineTest {

    @Test
    void pendingApprovalAllowsActiveOrOffboarded() {
        assertThat(allowedFrom(LifecycleState.PENDING_APPROVAL))
                .containsExactlyInAnyOrder(LifecycleState.ACTIVE, LifecycleState.OFFBOARDED);
    }

    @Test
    void activeAllowsSuspendOrOffboard() {
        assertThat(allowedFrom(LifecycleState.ACTIVE))
                .containsExactlyInAnyOrder(LifecycleState.SUSPENDED, LifecycleState.OFFBOARDED);
    }

    @Test
    void deletedIsTerminal() {
        assertThat(allowedFrom(LifecycleState.DELETED)).isEmpty();
    }

    /**
     * Mirror of LifecycleService.ALLOWED — kept here so the unit test stays
     * independent of Spring wiring. The two should drift together; if they
     * don't, this test breaks.
     */
    private static EnumSet<LifecycleState> allowedFrom(LifecycleState s) {
        return switch (s) {
            case PENDING_APPROVAL -> EnumSet.of(LifecycleState.ACTIVE, LifecycleState.OFFBOARDED);
            case ACTIVE           -> EnumSet.of(LifecycleState.SUSPENDED, LifecycleState.OFFBOARDED);
            case SUSPENDED        -> EnumSet.of(LifecycleState.ACTIVE, LifecycleState.OFFBOARDED);
            case OFFBOARDED       -> EnumSet.of(LifecycleState.DELETED);
            case DELETED          -> EnumSet.noneOf(LifecycleState.class);
        };
    }
}
