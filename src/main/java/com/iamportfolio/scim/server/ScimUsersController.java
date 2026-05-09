package com.iamportfolio.scim.server;

import com.iamportfolio.common.audit.Auditable;
import com.iamportfolio.identity.model.LifecycleState;
import com.iamportfolio.identity.model.User;
import com.iamportfolio.identity.repository.UserRepository;
import com.iamportfolio.scim.dto.ScimListResponse;
import com.iamportfolio.scim.dto.ScimUserResource;
import com.iamportfolio.scim.service.ScimMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SCIM 2.0 /Users endpoints (RFC 7644 §3.4 + §3.5).
 * Auth via OAuth2 bearer token with scope <code>scim:provision</code>
 * (validated by SecurityConfig).
 *
 * Filter support is intentionally minimal: only <code>userName eq "x"</code>
 * is parsed — enough for the connector's lookup pattern, and the most
 * common filter consumers (Okta, Azure AD) probe with first.
 */
@RestController
@RequestMapping(value = "/scim/v2/Users", produces = "application/scim+json")
public class ScimUsersController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScimMappingService mapper;

    @GetMapping
    public ScimListResponse<ScimUserResource> list(@RequestParam(value = "filter", required = false) String filter,
                                                   @RequestParam(value = "startIndex", defaultValue = "1") int startIndex) {
        List<User> users;
        if (filter != null && !filter.isBlank()) {
            String userName = parseUserNameEqFilter(filter);
            if (userName == null) {
                throw new IllegalArgumentException("Only 'userName eq \"…\"' filter is supported");
            }
            users = userRepository.findByUsername(userName).map(List::of).orElse(List.of());
        } else {
            users = userRepository.findAll();
        }
        List<ScimUserResource> mapped = users.stream().map(mapper::toScim).toList();
        return ScimListResponse.of(mapped, startIndex);
    }

    @GetMapping("/{id}")
    public ScimUserResource get(@PathVariable Long id) {
        return mapper.toScim(loadOrThrow(id));
    }

    @PostMapping(consumes = {"application/scim+json", "application/json"})
    @Auditable(value = "SCIM_USER_CREATE", resourceType = "USER")
    public ResponseEntity<ScimUserResource> create(@RequestBody ScimUserResource req) {
        if (req.userName() == null || req.userName().isBlank()) {
            throw new IllegalArgumentException("userName is required");
        }
        if (userRepository.existsByUsername(req.userName())) {
            throw new IllegalStateException("userName already exists: " + req.userName());
        }
        User user = new User();
        mapper.applyToEntity(req, user);
        // SCIM-created users land active by default unless caller said otherwise.
        if (user.getLifecycleState() == null) user.setLifecycleState(LifecycleState.ACTIVE);
        // SCIM POST does not carry a credential; set a random unusable password.
        user.setPassword("{noop}!" + java.util.UUID.randomUUID());
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toScim(saved));
    }

    @PutMapping(value = "/{id}", consumes = {"application/scim+json", "application/json"})
    @Auditable(value = "SCIM_USER_UPDATE", resourceType = "USER")
    public ScimUserResource replace(@PathVariable Long id, @RequestBody ScimUserResource req) {
        User user = loadOrThrow(id);
        mapper.applyToEntity(req, user);
        return mapper.toScim(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    @Auditable(value = "SCIM_USER_DELETE", resourceType = "USER")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = loadOrThrow(id);
        // Soft-delete via lifecycle so audit + provisioning hooks fire elsewhere.
        user.setLifecycleState(LifecycleState.OFFBOARDED);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handle(RuntimeException e) {
        boolean conflict = e instanceof IllegalStateException;
        return ResponseEntity.status(conflict ? 409 : 400).body(Map.of(
                "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:Error"),
                "status", conflict ? "409" : "400",
                "detail", e.getMessage()
        ));
    }

    private User loadOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    /**
     * Naive parser for the only filter shape we support: <code>userName eq "value"</code>.
     * Returns the literal value, or null if the input doesn't match.
     */
    static String parseUserNameEqFilter(String filter) {
        String trimmed = filter.trim();
        if (!trimmed.toLowerCase().startsWith("username eq ")) return null;
        String rest = trimmed.substring("userName eq ".length()).trim();
        if (rest.length() < 2 || rest.charAt(0) != '"' || rest.charAt(rest.length() - 1) != '"') return null;
        return rest.substring(1, rest.length() - 1);
    }
}
