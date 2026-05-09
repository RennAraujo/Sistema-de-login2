package com.iamportfolio.common.audit;

/**
 * Shared constants for the request-scoped correlation ID flowing through
 * the audit log, structured logs and outbound HTTP calls.
 */
public final class CorrelationIdConstants {

    /** HTTP header carrying the correlation ID across services. */
    public static final String HEADER = "X-Correlation-Id";

    /** SLF4J MDC key — also used by the JSON log encoder. */
    public static final String MDC_KEY = "correlationId";

    private CorrelationIdConstants() {
    }
}
