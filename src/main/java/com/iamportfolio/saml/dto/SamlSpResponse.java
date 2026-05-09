package com.iamportfolio.saml.dto;

import com.iamportfolio.saml.model.SamlServiceProvider;

import java.time.LocalDateTime;

public record SamlSpResponse(
        Long id,
        String entityId,
        String name,
        String acsUrl,
        String nameIdFormat,
        boolean enabled,
        boolean hasSigningCert,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SamlSpResponse from(SamlServiceProvider sp) {
        return new SamlSpResponse(
                sp.getId(), sp.getEntityId(), sp.getName(), sp.getAcsUrl(),
                sp.getNameIdFormat(), sp.isEnabled(),
                sp.getSigningCertPem() != null && !sp.getSigningCertPem().isBlank(),
                sp.getCreatedAt(), sp.getUpdatedAt()
        );
    }
}
