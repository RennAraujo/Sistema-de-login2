package com.iamportfolio.saml.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Registered SAML 2.0 Service Provider (relying party). The IdP only
 * issues assertions for SPs in this table — anything else is rejected
 * at /saml2/idp/sso.
 */
@Entity
@Table(name = "saml_service_providers")
public class SamlServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_id", nullable = false, unique = true, length = 512)
    private String entityId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "acs_url", nullable = false, length = 2048)
    private String acsUrl;

    @Column(name = "name_id_format", length = 255)
    private String nameIdFormat;

    @Column(name = "signing_cert_pem", columnDefinition = "TEXT")
    private String signingCertPem;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAcsUrl() { return acsUrl; }
    public void setAcsUrl(String acsUrl) { this.acsUrl = acsUrl; }
    public String getNameIdFormat() { return nameIdFormat; }
    public void setNameIdFormat(String nameIdFormat) { this.nameIdFormat = nameIdFormat; }
    public String getSigningCertPem() { return signingCertPem; }
    public void setSigningCertPem(String signingCertPem) { this.signingCertPem = signingCertPem; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
