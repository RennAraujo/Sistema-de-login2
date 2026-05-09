package com.iamportfolio.audit.dto;

import com.iamportfolio.common.audit.AuditEvent;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditEventResponse(
        Long id,
        LocalDateTime timestamp,
        String actor,
        String actorIp,
        String action,
        String resourceType,
        String resourceId,
        String outcome,
        Map<String, Object> details,
        String correlationId
) {
    public static AuditEventResponse from(AuditEvent e) {
        return new AuditEventResponse(
                e.getId(), e.getTimestamp(), e.getActor(), e.getActorIp(),
                e.getAction(), e.getResourceType(), e.getResourceId(),
                e.getOutcome(), e.getDetails(), e.getCorrelationId()
        );
    }
}
