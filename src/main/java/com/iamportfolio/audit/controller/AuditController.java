package com.iamportfolio.audit.controller;

import com.iamportfolio.audit.dto.AuditEventResponse;
import com.iamportfolio.common.audit.AuditEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit/events")
public class AuditController {

    @Autowired
    private AuditEventRepository repository;

    /**
     * Paginated, filtered audit search. Access is gated in SecurityConfig
     * by the audit:read permission (or ROLE_AUDITOR / ROLE_ADMIN).
     */
    @GetMapping
    public Page<AuditEventResponse> search(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        return repository.search(actor, action, from, to, pageable)
                .map(AuditEventResponse::from);
    }
}
