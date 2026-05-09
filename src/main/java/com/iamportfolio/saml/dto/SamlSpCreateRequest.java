package com.iamportfolio.saml.dto;

import jakarta.validation.constraints.NotBlank;

public record SamlSpCreateRequest(
        @NotBlank String entityId,
        @NotBlank String name,
        @NotBlank String acsUrl,
        String nameIdFormat,
        String signingCertPem
) {
}
