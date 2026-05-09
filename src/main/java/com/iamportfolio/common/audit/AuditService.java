package com.iamportfolio.common.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Writes audit events. Always REQUIRES_NEW so a failing business
 * transaction still produces its FAILURE audit row.
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditEventRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String resourceType, String resourceId, String outcome,
                       Map<String, Object> details) {
        try {
            AuditEvent event = new AuditEvent();
            event.setAction(action);
            event.setResourceType(emptyToNull(resourceType));
            event.setResourceId(emptyToNull(resourceId));
            event.setOutcome(outcome);
            event.setActor(currentActor());
            event.setActorIp(currentIp());
            event.setCorrelationId(MDC.get(CorrelationIdConstants.MDC_KEY));
            if (details != null) {
                event.getDetails().putAll(details);
            }
            repository.save(event);
        } catch (Exception e) {
            // Never let auditing break the business flow — log and move on.
            logger.warn("Failed to write audit event {}/{}: {}", action, outcome, e.getMessage());
        }
    }

    private static String currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        return auth.getName();
    }

    private static String currentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String forwarded = req.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return req.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
