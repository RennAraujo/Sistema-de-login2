package com.login.controller;

import com.login.dto.*;
import com.login.service.AuthService;
import com.login.service.TwoFactorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private TwoFactorService twoFactorService;

    /**
     * Endpoint para registro de usuário
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Requisição de registro recebida para: {}", request.getUsername());
        
        try {
            AuthResponse response = authService.register(request);
            
            if (response.getAccessToken() != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Erro no registro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Requisição de login recebida para: {}", request.getUsernameOrEmail());
        
        try {
            AuthResponse response = authService.login(request);
            
            if (response.getAccessToken() != null) {
                return ResponseEntity.ok(response);
            } else if (response.isRequiresTwoFactor()) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            logger.error("Erro no login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para configurar 2FA
     */
    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setupTwoFactor() {
        logger.info("Requisição de configuração 2FA recebida");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            TwoFactorSetupResponse response = authService.setupTwoFactor(username);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Erro na configuração 2FA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TwoFactorSetupResponse.error("Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para confirmar configuração do 2FA
     */
    @PostMapping("/2fa/confirm")
    public ResponseEntity<?> confirmTwoFactor(@RequestBody Map<String, String> request) {
        logger.info("Requisição de confirmação 2FA recebida");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String code = request.get("code");
            
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.error("Código é obrigatório"));
            }
            
            AuthResponse response = authService.confirmTwoFactor(username, code);
            
            if (response.getMessage() != null && !response.getMessage().contains("inválido")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Erro na confirmação 2FA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para desabilitar 2FA
     */
    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disableTwoFactor(@RequestBody Map<String, String> request) {
        logger.info("Requisição de desabilitação 2FA recebida");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String code = request.get("code");
            
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.error("Código é obrigatório"));
            }
            
            AuthResponse response = authService.disableTwoFactor(username, code);
            
            if (response.getMessage() != null && !response.getMessage().contains("inválido")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Erro na desabilitação 2FA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para verificar status do usuário
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            return authService.getUserByUsername(username)
                    .map(user -> ResponseEntity.ok(Map.of(
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                            "lastName", user.getLastName() != null ? user.getLastName() : "",
                            "twoFactorEnabled", user.isTwoFactorEnabled(),
                            "lastLogin", user.getLastLogin()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Erro ao obter usuário atual: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para validar se token é válido
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "username", authentication.getName()
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }
        } catch (Exception e) {
            logger.error("Erro na validação do token: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    /**
     * Endpoint de logout (limpa contexto)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso"));
        } catch (Exception e) {
            logger.error("Erro no logout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }
} 