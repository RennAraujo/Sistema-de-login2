package com.iamportfolio.scim.outbound;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "provisioning_events")
public class ProvisioningEvent {

    public enum Operation { CREATE, UPDATE, DELETE }
    public enum Outcome   { SUCCESS, FAILURE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "external_id", length = 128)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 32)
    private Operation operation;

    @Column(name = "target", nullable = false, length = 128)
    private String target;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 16)
    private Outcome outcome;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt = LocalDateTime.now();

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public Operation getOperation() { return operation; }
    public void setOperation(Operation operation) { this.operation = operation; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public Outcome getOutcome() { return outcome; }
    public void setOutcome(Outcome outcome) { this.outcome = outcome; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
