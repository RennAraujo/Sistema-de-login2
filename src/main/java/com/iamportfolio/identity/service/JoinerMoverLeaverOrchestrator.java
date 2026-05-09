package com.iamportfolio.identity.service;

import com.iamportfolio.identity.event.LifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Hook for joiner-mover-leaver downstream side effects (notifications,
 * provisioning, ticket creation). Today it just logs the transition; the
 * SCIM outbound provisioning client (commit 5.3) plugs in here.
 */
@Component
public class JoinerMoverLeaverOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(JoinerMoverLeaverOrchestrator.class);

    @EventListener
    public void onLifecycleTransition(LifecycleEvent event) {
        logger.info("[JML] {} {}->{} (triggered by {})",
                event.getUser().getUsername(),
                event.getFromState(),
                event.getToState(),
                event.getTriggeredBy());
        // Future hooks: outbound SCIM provisioning, notify manager, revoke tokens.
    }
}
