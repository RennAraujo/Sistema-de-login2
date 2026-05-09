package com.iamportfolio.identity.event;

import com.iamportfolio.identity.model.LifecycleState;
import com.iamportfolio.identity.model.User;
import org.springframework.context.ApplicationEvent;

/**
 * Published whenever a user transitions between lifecycle states.
 * Listeners (provisioning, audit, notification) react asynchronously
 * so the orchestrating transaction stays focused.
 */
public class LifecycleEvent extends ApplicationEvent {

    private final User user;
    private final LifecycleState fromState;
    private final LifecycleState toState;
    private final String triggeredBy;

    public LifecycleEvent(Object source, User user, LifecycleState fromState,
                          LifecycleState toState, String triggeredBy) {
        super(source);
        this.user = user;
        this.fromState = fromState;
        this.toState = toState;
        this.triggeredBy = triggeredBy;
    }

    public User getUser() { return user; }
    public LifecycleState getFromState() { return fromState; }
    public LifecycleState getToState() { return toState; }
    public String getTriggeredBy() { return triggeredBy; }
}
