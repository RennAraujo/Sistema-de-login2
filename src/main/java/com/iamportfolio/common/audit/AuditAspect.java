package com.iamportfolio.common.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps every {@link Auditable} method invocation with a SUCCESS / FAILURE
 * audit_event. Exceptions are re-thrown so business behavior is unchanged.
 */
@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditService auditService;

    @Around("@annotation(com.iamportfolio.common.audit.Auditable)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Auditable annotation = method.getAnnotation(Auditable.class);
        String action = annotation.value();
        String resourceType = annotation.resourceType();

        Map<String, Object> details = new HashMap<>();
        details.put("method", sig.toShortString());

        try {
            Object result = pjp.proceed();
            auditService.record(action, resourceType, null, "SUCCESS", details);
            return result;
        } catch (Throwable t) {
            details.put("error", t.getClass().getSimpleName());
            details.put("errorMessage", t.getMessage());
            auditService.record(action, resourceType, null, "FAILURE", details);
            throw t;
        }
    }
}
