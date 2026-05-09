package com.iamportfolio.scim.outbound;

import com.iamportfolio.common.audit.CorrelationIdConstants;
import com.iamportfolio.identity.event.LifecycleEvent;
import com.iamportfolio.identity.model.LifecycleState;
import com.iamportfolio.identity.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Listens for joiner-mover-leaver events and pushes them out to the
 * external SCIM connector. Each attempt produces a row in
 * provisioning_events for replay / forensics, regardless of outcome.
 *
 * Runs on a separate transaction so a failure here doesn't roll back
 * the upstream lifecycle transition (which already committed in
 * LifecycleService).
 */
@Component
public class ProvisioningOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(ProvisioningOrchestrator.class);
    private static final String TARGET = "scim-connector";

    @Autowired
    private ScimProvisioningClient client;

    @Autowired
    private ProvisioningEventRepository repository;

    @EventListener
    @Async
    public void onLifecycleTransition(LifecycleEvent event) {
        User user = event.getUser();
        LifecycleState target = event.getToState();

        if (target == LifecycleState.ACTIVE) {
            tryProvision(user, ProvisioningEvent.Operation.UPDATE,
                    () -> client.provision(externalIdOf(user), user.getUsername(),
                            user.getEmail(), true));
        } else if (target == LifecycleState.SUSPENDED) {
            tryProvision(user, ProvisioningEvent.Operation.UPDATE,
                    () -> client.provision(externalIdOf(user), user.getUsername(),
                            user.getEmail(), false));
        } else if (target == LifecycleState.OFFBOARDED) {
            tryProvision(user, ProvisioningEvent.Operation.DELETE,
                    () -> client.deprovision(externalIdOf(user)));
        }
        // PENDING_APPROVAL / DELETED: no outbound provisioning yet.
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void tryProvision(User user, ProvisioningEvent.Operation op, Runnable action) {
        ProvisioningEvent ev = new ProvisioningEvent();
        ev.setUserId(user.getId());
        ev.setExternalId(externalIdOf(user));
        ev.setOperation(op);
        ev.setTarget(TARGET);
        ev.setCorrelationId(MDC.get(CorrelationIdConstants.MDC_KEY));
        try {
            action.run();
            ev.setOutcome(ProvisioningEvent.Outcome.SUCCESS);
            ev.setHttpStatus(200);
            logger.info("Provisioned {} ({}) -> {}: SUCCESS", user.getUsername(), op, TARGET);
        } catch (WebClientResponseException e) {
            ev.setOutcome(ProvisioningEvent.Outcome.FAILURE);
            ev.setHttpStatus(e.getStatusCode().value());
            ev.setErrorMessage(e.getMessage());
            logger.warn("Provisioning {} ({}) failed with HTTP {}: {}",
                    user.getUsername(), op, e.getStatusCode().value(), e.getMessage());
        } catch (Exception e) {
            ev.setOutcome(ProvisioningEvent.Outcome.FAILURE);
            ev.setErrorMessage(e.getMessage());
            logger.warn("Provisioning {} ({}) failed: {}", user.getUsername(), op, e.getMessage());
        } finally {
            repository.save(ev);
        }
    }

    private static String externalIdOf(User user) {
        return user.getExternalId() != null ? user.getExternalId() : String.valueOf(user.getId());
    }
}
