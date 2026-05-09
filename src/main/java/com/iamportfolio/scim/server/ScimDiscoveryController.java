package com.iamportfolio.scim.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * SCIM 2.0 discovery endpoints (RFC 7644 §4): ServiceProviderConfig,
 * Schemas, ResourceTypes. Connectors call these on first contact to
 * negotiate which features they can use.
 */
@RestController
@RequestMapping(value = "/scim/v2", produces = "application/scim+json")
public class ScimDiscoveryController {

    @GetMapping("/ServiceProviderConfig")
    public Map<String, Object> serviceProviderConfig() {
        return Map.of(
                "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"),
                "documentationUri", "https://github.com/RennAraujo/Sistema-de-login2",
                "patch",  Map.of("supported", false),
                "bulk",   Map.of("supported", false, "maxOperations", 0, "maxPayloadSize", 0),
                "filter", Map.of("supported", true, "maxResults", 100),
                "changePassword",  Map.of("supported", false),
                "sort",   Map.of("supported", false),
                "etag",   Map.of("supported", false),
                "authenticationSchemes", List.of(Map.of(
                        "type", "oauthbearertoken",
                        "name", "OAuth Bearer Token",
                        "description", "OAuth2 Bearer Token (scope scim:provision)",
                        "primary", true
                ))
        );
    }

    @GetMapping("/ResourceTypes")
    public List<Map<String, Object>> resourceTypes() {
        return List.of(
                Map.of(
                        "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ResourceType"),
                        "id", "User",
                        "name", "User",
                        "endpoint", "/Users",
                        "schema", "urn:ietf:params:scim:schemas:core:2.0:User"
                ),
                Map.of(
                        "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ResourceType"),
                        "id", "Group",
                        "name", "Group",
                        "endpoint", "/Groups",
                        "schema", "urn:ietf:params:scim:schemas:core:2.0:Group"
                )
        );
    }

    @GetMapping("/Schemas")
    public List<Map<String, Object>> schemas() {
        return List.of(
                Map.of(
                        "id", "urn:ietf:params:scim:schemas:core:2.0:User",
                        "name", "User",
                        "description", "User Account"
                ),
                Map.of(
                        "id", "urn:ietf:params:scim:schemas:core:2.0:Group",
                        "name", "Group",
                        "description", "Group"
                )
        );
    }
}
