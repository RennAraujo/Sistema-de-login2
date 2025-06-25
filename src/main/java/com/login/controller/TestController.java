package com.login.controller;

import com.login.model.User;
import com.login.repository.UserRepository;
import com.login.service.TwoFactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwoFactorService twoFactorService;

    /**
     * Endpoint público para demonstrar funcionalidades para recrutador
     */
    @GetMapping("/demo")
    public ResponseEntity<?> getDemoInfo() {
        Map<String, Object> demoInfo = new HashMap<>();
        
        demoInfo.put("sistema", "Sistema de Login Seguro com 2FA");
        demoInfo.put("tecnologias", List.of(
            "Spring Boot 3.2.0",
            "Spring Security 6",
            "JWT (JSON Web Tokens)",
            "TOTP (Time-based One-Time Password)",
            "H2 Database",
            "BCrypt Password Encoding",
            "QR Code Generation"
        ));
        
        demoInfo.put("funcionalidades", List.of(
            "Registro de usuários com validação",
            "Login seguro com JWT",
            "Autenticação de duas etapas (2FA)",
            "Geração de QR Code para configuração 2FA",
            "Códigos de backup para 2FA",
            "Validação de tokens JWT",
            "Proteção CORS configurada",
            "Rate limiting e outras medidas de segurança"
        ));
        
        demoInfo.put("endpoints", Map.of(
            "registro", "POST /api/auth/register",
            "login", "POST /api/auth/login",
            "setup2FA", "POST /api/auth/2fa/setup",
            "confirm2FA", "POST /api/auth/2fa/confirm",
            "perfil", "GET /api/auth/me",
            "validarToken", "GET /api/auth/validate"
        ));
        
        demoInfo.put("timestamp", LocalDateTime.now());
        demoInfo.put("status", "Operacional");
        
        return ResponseEntity.ok(demoInfo);
    }

    /**
     * Endpoint para obter estatísticas básicas do sistema
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countActiveUsers();
            long usersWithTwoFactor = userRepository.findUsersWithTwoFactorEnabled().size();
            
            stats.put("totalUsuarios", totalUsers);
            stats.put("usuariosAtivos", activeUsers);
            stats.put("usuariosCom2FA", usersWithTwoFactor);
            stats.put("percentual2FA", totalUsers > 0 ? (usersWithTwoFactor * 100.0 / totalUsers) : 0);
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", "Não foi possível obter estatísticas",
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Endpoint para gerar um código TOTP de exemplo (demonstração)
     */
    @GetMapping("/totp-demo")
    public ResponseEntity<?> getTotpDemo() {
        try {
            String demoSecret = twoFactorService.generateSecret();
            String currentCode = twoFactorService.getCurrentCode(demoSecret);
            String qrUrl = twoFactorService.generateQrCodeUrl(demoSecret, "demo@example.com");
            
            Map<String, Object> demo = new HashMap<>();
            demo.put("secret", demoSecret);
            demo.put("currentCode", currentCode);
            demo.put("qrUrl", qrUrl);
            demo.put("explicacao", "Este é um exemplo de como o 2FA funciona. " +
                    "O código muda a cada 30 segundos e pode ser escaneado por apps como Google Authenticator.");
            demo.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(demo);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", "Erro ao gerar demonstração TOTP",
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Endpoint para demonstrar validação de código TOTP
     */
    @PostMapping("/verify-totp")
    public ResponseEntity<?> verifyTotpDemo(@RequestBody Map<String, String> request) {
        try {
            String secret = request.get("secret");
            String code = request.get("code");
            
            if (secret == null || code == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Secret e code são obrigatórios",
                    "timestamp", LocalDateTime.now()
                ));
            }
            
            boolean isValid = twoFactorService.verifyCode(secret, code);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);
            result.put("message", isValid ? "Código válido!" : "Código inválido ou expirado");
            result.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", "Erro ao verificar código",
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Endpoint para health check do sistema
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Verificar conexão com banco
            long userCount = userRepository.count();
            health.put("database", "OK");
            health.put("userCount", userCount);
            
            // Verificar serviço 2FA
            String testSecret = twoFactorService.generateSecret();
            health.put("twoFactorService", testSecret != null ? "OK" : "ERROR");
            
            health.put("status", "Healthy");
            health.put("timestamp", LocalDateTime.now());
            health.put("uptime", "Sistema operacional");
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "Unhealthy");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(health);
        }
    }

    /**
     * Endpoint com instruções para teste
     */
    @GetMapping("/instructions")
    public ResponseEntity<?> getTestInstructions() {
        Map<String, Object> instructions = new HashMap<>();
        
        instructions.put("titulo", "Instruções para Teste do Sistema");
        
        instructions.put("passos", List.of(
            "1. Acesse a interface web em http://localhost:8080",
            "2. Registre um novo usuário na aba 'Registro'",
            "3. Faça login com as credenciais criadas",
            "4. Configure o 2FA na área autenticada",
            "5. Escaneie o QR Code com Google Authenticator ou similar",
            "6. Teste o login com 2FA habilitado",
            "7. Explore os endpoints da API em /api/auth/*"
        ));
        
        instructions.put("credenciaisDemo", Map.of(
            "observacao", "Você pode criar suas próprias credenciais ou usar as funcionalidades de demonstração",
            "dica", "Use /api/test/totp-demo para ver como o 2FA funciona sem precisar configurar"
        ));
        
        instructions.put("endpoints", Map.of(
            "demo", "GET /api/test/demo - Informações do sistema",
            "stats", "GET /api/test/stats - Estatísticas",
            "health", "GET /api/test/health - Status do sistema",
            "totpDemo", "GET /api/test/totp-demo - Demonstração TOTP"
        ));
        
        instructions.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(instructions);
    }
} 