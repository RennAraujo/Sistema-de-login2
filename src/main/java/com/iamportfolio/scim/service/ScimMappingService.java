package com.iamportfolio.scim.service;

import com.iamportfolio.identity.model.LifecycleState;
import com.iamportfolio.identity.model.User;
import com.iamportfolio.scim.dto.ScimUserResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Maps between the internal {@link User} entity and the SCIM 2.0
 * {@link ScimUserResource} representation. Kept stateless on purpose so
 * both the inbound server (ScimUsersController) and the outbound
 * provisioning client (commit 5.3) can share it.
 */
@Component
public class ScimMappingService {

    private static final String USER_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:User";

    public ScimUserResource toScim(User user) {
        return new ScimUserResource(
                List.of(USER_SCHEMA),
                String.valueOf(user.getId()),
                user.getExternalId(),
                user.getUsername(),
                new ScimUserResource.Name(user.getFirstName(), user.getLastName(),
                        joinName(user.getFirstName(), user.getLastName())),
                user.getEmail() == null ? null : List.of(
                        new ScimUserResource.Email(user.getEmail(), "work", true)
                ),
                user.getLifecycleState() == LifecycleState.ACTIVE,
                Map.of(
                        "resourceType", "User",
                        "created", user.getCreatedAt() == null ? "" : user.getCreatedAt().toString(),
                        "lastModified", user.getUpdatedAt() == null ? "" : user.getUpdatedAt().toString()
                )
        );
    }

    public void applyToEntity(ScimUserResource src, User target) {
        if (src.userName() != null) target.setUsername(src.userName());
        if (src.externalId() != null) target.setExternalId(src.externalId());
        if (src.name() != null) {
            if (src.name().givenName() != null) target.setFirstName(src.name().givenName());
            if (src.name().familyName() != null) target.setLastName(src.name().familyName());
        }
        if (src.emails() != null && !src.emails().isEmpty()) {
            target.setEmail(src.emails().stream()
                    .filter(ScimUserResource.Email::primary)
                    .findFirst().orElse(src.emails().get(0))
                    .value());
        }
        // Active flag flips lifecycle: true -> ACTIVE, false -> SUSPENDED.
        // Explicit OFFBOARDED transitions go through LifecycleService instead.
        if (src.active()) {
            target.setLifecycleState(LifecycleState.ACTIVE);
        } else if (target.getLifecycleState() == LifecycleState.ACTIVE) {
            target.setLifecycleState(LifecycleState.SUSPENDED);
        }
    }

    private static String joinName(String first, String last) {
        if (first == null && last == null) return null;
        return ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
    }
}
