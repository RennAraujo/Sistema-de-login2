package com.iamportfolio.scim.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * SCIM 2.0 User resource representation (RFC 7643).
 * Only the attributes the connector needs are mapped here; extension
 * via "schemas" + extension URN attributes works through the generic
 * Map fields the JSON encoder accepts on demand.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScimUserResource(
        List<String> schemas,
        String id,
        String externalId,
        String userName,
        Name name,
        List<Email> emails,
        boolean active,
        Map<String, Object> meta
) {
    public record Name(String givenName, String familyName, String formatted) {}
    public record Email(String value, String type, boolean primary) {}
}
