package com.iamportfolio.identity.controller;

import com.iamportfolio.identity.dto.UserResponse;
import com.iamportfolio.identity.model.User;
import com.iamportfolio.identity.repository.UserRepository;
import com.iamportfolio.identity.service.LifecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Identity administration endpoints. Gated by identity:read / identity:write
 * in SecurityConfig. Lifecycle transitions are exposed as POST verbs so they
 * stay distinct from generic PUT updates.
 */
@RestController
@RequestMapping("/api/identity/users")
public class UserAdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LifecycleService lifecycleService;

    @GetMapping
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(loadOrThrow(id));
    }

    @PostMapping("/{id}/approve")
    public UserResponse approve(@PathVariable Long id) {
        return UserResponse.from(handle(() -> lifecycleService.approve(id)));
    }

    @PostMapping("/{id}/suspend")
    public UserResponse suspend(@PathVariable Long id) {
        return UserResponse.from(handle(() -> lifecycleService.suspend(id)));
    }

    @PostMapping("/{id}/reactivate")
    public UserResponse reactivate(@PathVariable Long id) {
        return UserResponse.from(handle(() -> lifecycleService.reactivate(id)));
    }

    @PostMapping("/{id}/offboard")
    public UserResponse offboard(@PathVariable Long id) {
        return UserResponse.from(handle(() -> lifecycleService.offboard(id)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> notFound(IllegalArgumentException e) {
        return ResponseEntity.status(NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    private User loadOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found: " + id));
    }

    private static User handle(java.util.function.Supplier<User> action) {
        try {
            return action.get();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        }
    }
}
