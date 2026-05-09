package com.iamportfolio.scim.server;

import com.iamportfolio.rbac.model.Group;
import com.iamportfolio.rbac.repository.GroupRepository;
import com.iamportfolio.scim.dto.ScimListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SCIM 2.0 /Groups read endpoints. Read-only for now (mutations land
 * on /api/rbac/groups via the dedicated admin API in a follow-up
 * commit). Connectors that probe Groups during discovery are happy
 * with this surface.
 */
@RestController
@RequestMapping(value = "/scim/v2/Groups", produces = "application/scim+json")
public class ScimGroupsController {

    @Autowired
    private GroupRepository groupRepository;

    @GetMapping
    public ScimListResponse<Map<String, Object>> list() {
        List<Map<String, Object>> mapped = groupRepository.findAll().stream()
                .map(ScimGroupsController::toScim).toList();
        return ScimListResponse.of(mapped, 1);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Group g = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));
        return toScim(g);
    }

    private static Map<String, Object> toScim(Group g) {
        return Map.of(
                "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:Group"),
                "id", String.valueOf(g.getId()),
                "displayName", g.getName(),
                "meta", Map.of("resourceType", "Group")
        );
    }
}
