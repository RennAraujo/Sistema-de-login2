package com.iamportfolio.oauth2.service;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Maps technical OAuth2 scope names to human-friendly descriptions
 * shown on the consent screen. Unknown scopes fall back to their raw
 * value so the user still sees what they're granting.
 */
@Component
public class ScopeDescriptionRegistry {

    private static final Map<String, String> DESCRIPTIONS = Map.of(
            "openid",          "Verify your identity",
            "profile",         "Read your basic profile (name, username)",
            "email",           "Read your email address",
            "scim:provision",  "Provision and manage identities on your behalf",
            "identity:read",   "Read user and group information",
            "identity:write",  "Create and update users and groups",
            "audit:read",      "Read the audit log",
            "governance:manage","Manage access reviews and SoD rules"
    );

    public String describe(String scope) {
        return DESCRIPTIONS.getOrDefault(scope, scope);
    }
}
