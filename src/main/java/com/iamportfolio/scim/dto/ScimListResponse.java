package com.iamportfolio.scim.dto;

import java.util.List;

/**
 * Standard SCIM 2.0 ListResponse envelope (RFC 7644 §3.4.2).
 */
public record ScimListResponse<T>(
        List<String> schemas,
        int totalResults,
        int startIndex,
        int itemsPerPage,
        List<T> Resources
) {
    public static <T> ScimListResponse<T> of(List<T> items, int startIndex) {
        return new ScimListResponse<>(
                List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
                items.size(), startIndex, items.size(), items
        );
    }
}
