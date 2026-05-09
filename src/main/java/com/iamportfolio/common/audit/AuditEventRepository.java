package com.iamportfolio.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    @Query("SELECT a FROM AuditEvent a " +
           "WHERE (:actor IS NULL OR a.actor = :actor) " +
           "AND (:action IS NULL OR a.action = :action) " +
           "AND (:from IS NULL OR a.timestamp >= :from) " +
           "AND (:to   IS NULL OR a.timestamp <= :to) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditEvent> search(@Param("actor") String actor,
                            @Param("action") String action,
                            @Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to,
                            Pageable pageable);
}
