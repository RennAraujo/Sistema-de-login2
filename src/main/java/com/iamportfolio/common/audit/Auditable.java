package com.iamportfolio.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method whose invocation should produce an AuditEvent. The
 * {@link AuditAspect} captures actor (from SecurityContext), source IP
 * (from the current request) and outcome (SUCCESS / FAILURE based on
 * thrown exceptions), and writes a row into audit_events.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {

    /** Action name in the canonical taxonomy, e.g. "AUTH_LOGIN". */
    String value();

    /** Optional resource type, e.g. "USER" or "OAUTH2_CLIENT". */
    String resourceType() default "";
}
