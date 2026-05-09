package com.iamportfolio.oauth2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record OAuth2ClientCreateRequest(
        @NotBlank String clientId,
        @NotBlank String clientName,
        @NotEmpty Set<String> grantTypes,
        @NotEmpty Set<String> scopes,
        Set<String> redirectUris,
        boolean requireConsent
) {
}
