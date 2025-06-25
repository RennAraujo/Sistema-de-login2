package com.login.controller;

import com.login.model.User;
import com.login.repository.UserRepository;
import com.login.repository.supermarket.*;
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

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    /**
     * Endpoint público para demonstrar funcionalidades para recrutador
     */
    @GetMapping("/demo")
    public ResponseEntity<?> getDemoInfo() {
        Map<String, Object> demoInfo = new HashMap<>();
        
        demoInfo.put("sistema", "Sistema de Login Seguro com 2FA e Gestão de Supermercado");
        demoInfo.put("tecnologias", List.of(
            "Spring Boot 3.2.0",
            "Spring Security 6",
            "JWT (JSON Web Tokens)",
            "TOTP (Time-based One-Time Password)",
            "H2 Database",
            "BCrypt Password Encoding",
            "QR Code Generation",
            "Swagger/OpenAPI 3.0",
            "Spring Data JPA",
            "Bean Validation"
        ));
        
        demoInfo.put("funcionalidadesAuth", List.of(
            "Registro de usuários com validação",
            "Login seguro com JWT",
            "Autenticação de duas etapas (2FA)",
            "Geração de QR Code para configuração 2FA",
            "Códigos de backup para 2FA",
            "Validação de tokens JWT",
            "Proteção CORS configurada"
        ));
        
        demoInfo.put("funcionalidadesSupermercado", List.of(
            "Gestão completa de produtos",
            "Categorização de produtos",
            "Controle de estoque em tempo real",
            "Gestão de fornecedores",
            "Alertas de estoque baixo",
            "Controle de validade de produtos",
            "Produtos em promoção",
            "Busca avançada por vários critérios",
            "Relatórios de estoque"
        ));
        
        demoInfo.put("endpointsAuth", Map.of(
            "registro", "POST /api/auth/register",
            "login", "POST /api/auth/login",
            "setup2FA", "POST /api/auth/2fa/setup",
            "confirm2FA", "POST /api/auth/2fa/confirm",
            "perfil", "GET /api/auth/me",
            "validarToken", "GET /api/auth/validate"
        ));
        
        demoInfo.put("endpointsSupermercado", Map.of(
            "categorias", "GET/POST/PUT/DELETE /api/supermercado/categorias",
            "produtos", "GET/POST/PUT/DELETE /api/supermercado/produtos",
            "estoque", "GET/POST/PUT/PATCH /api/supermercado/estoques",
            "promocoes", "GET /api/supermercado/produtos/promocao",
            "estoqueBaixo", "GET /api/supermercado/estoques/baixo",
            "swagger", "GET /swagger-ui.html"
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
            // Estatísticas do sistema de login
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countActiveUsers();
            long usersWithTwoFactor = userRepository.findUsersWithTwoFactorEnabled().size();
            
            stats.put("sistemaLogin", Map.of(
                "totalUsuarios", totalUsers,
                "usuariosAtivos", activeUsers,
                "usuariosCom2FA", usersWithTwoFactor,
                "percentual2FA", totalUsers > 0 ? (usersWithTwoFactor * 100.0 / totalUsers) : 0
            ));
            
            // Estatísticas do supermercado
            long totalCategorias = categoriaRepository.count();
            long categoriasAtivas = categoriaRepository.findByAtivaTrue().size();
            long totalProdutos = produtoRepository.count();
            long produtosAtivos = produtoRepository.findByAtivoTrue().size();
            long totalFornecedores = fornecedorRepository.count();
            long fornecedoresAtivos = fornecedorRepository.findByAtivoTrue().size();
            long produtosPromocao = produtoRepository.findProdutosEmPromocao().size();
            long estoqueBaixo = estoqueRepository.findEstoquesComNivelBaixo().size();
            long produtosVencidos = estoqueRepository.findProdutosVencidos().size();
            
            stats.put("supermercado", Map.of(
                "categorias", Map.of(
                    "total", totalCategorias,
                    "ativas", categoriasAtivas
                ),
                "produtos", Map.of(
                    "total", totalProdutos,
                    "ativos", produtosAtivos,
                    "emPromocao", produtosPromocao
                ),
                "fornecedores", Map.of(
                    "total", totalFornecedores,
                    "ativos", fornecedoresAtivos
                ),
                "alertas", Map.of(
                    "estoqueBaixo", estoqueBaixo,
                    "produtosVencidos", produtosVencidos
                )
            ));
            
            stats.put("timestamp", LocalDateTime.now());
            stats.put("status", "Operacional");
            
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
     * Endpoint para demonstrar funcionalidades do supermercado
     */
    @GetMapping("/supermercado-demo")
    public ResponseEntity<?> getSupermercadoDemo() {
        Map<String, Object> demo = new HashMap<>();
        
        try {
            demo.put("titulo", "Demonstração do Sistema de Supermercado");
            
            // Informações sobre as funcionalidades
            demo.put("funcionalidades", Map.of(
                "categorias", "Gestão completa de categorias de produtos",
                "produtos", "Cadastro detalhado com código de barras, preços e promoções",
                "estoque", "Controle em tempo real com alertas de nível baixo",
                "fornecedores", "Gestão de fornecedores com CNPJ e dados completos"
            ));
            
            // Exemplos de dados cadastrados
            demo.put("exemplosDados", Map.of(
                "categoriaExemplo", Map.of(
                    "nome", "Alimentos",
                    "descricao", "Produtos alimentícios em geral",
                    "ativa", true
                ),
                "produtoExemplo", Map.of(
                    "nome", "Arroz Branco Tipo 1 5kg",
                    "codigoBarras", "7891234567890",
                    "preco", 28.90,
                    "precoPromocional", 25.90,
                    "categoria", "Alimentos"
                ),
                "estoqueExemplo", Map.of(
                    "quantidadeAtual", 150,
                    "quantidadeMinima", 50,
                    "localizacao", "A1-P1",
                    "estoqueBaixo", false
                )
            ));
            
            // URLs para testar
            demo.put("urlsTeste", Map.of(
                "swagger", "http://localhost:8080/swagger-ui.html",
                "categorias", "GET /api/supermercado/categorias/ativas",
                "produtos", "GET /api/supermercado/produtos/ativos",
                "promocoes", "GET /api/supermercado/produtos/promocao",
                "estoqueBaixo", "GET /api/supermercado/estoques/baixo"
            ));
            
            demo.put("instrucoes", List.of(
                "1. Acesse o Swagger em /swagger-ui.html",
                "2. Faça login para obter o token JWT",
                "3. Use o botão 'Authorize' no Swagger com 'Bearer {token}'",
                "4. Teste os endpoints de categorias, produtos e estoque",
                "5. Veja os alertas de estoque baixo e produtos vencidos"
            ));
            
            demo.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(demo);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", "Erro ao gerar demonstração do supermercado",
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Endpoint para demonstrar alertas do supermercado
     */
    @GetMapping("/alertas-demo")
    public ResponseEntity<?> getAlertasDemo() {
        Map<String, Object> alertas = new HashMap<>();
        
        try {
            // Produtos com estoque baixo
            long estoqueBaixo = estoqueRepository.findEstoquesComNivelBaixo().size();
            long produtosVencidos = estoqueRepository.findProdutosVencidos().size();
            long produtosPromocao = produtoRepository.findProdutosEmPromocao().size();
            
            alertas.put("resumo", Map.of(
                "estoqueBaixo", estoqueBaixo,
                "produtosVencidos", produtosVencidos,
                "produtosPromocao", produtosPromocao
            ));
            
            alertas.put("tipos", Map.of(
                "critico", "Produtos vencidos - ação imediata necessária",
                "atencao", "Estoque baixo - reabastecer em breve",
                "oportunidade", "Produtos em promoção - destacar nas vendas"
            ));
            
            alertas.put("acoes", List.of(
                "Verificar produtos vencidos e remover das prateleiras",
                "Contatar fornecedores para produtos com estoque baixo",
                "Atualizar cartazes promocionais",
                "Configurar alertas automáticos por email/SMS"
            ));
            
            alertas.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(alertas);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", "Erro ao obter alertas",
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
            // Verificar conexão com banco - Sistema de Login
            long userCount = userRepository.count();
            health.put("sistemaLogin", Map.of(
                "database", "OK",
                "userCount", userCount
            ));
            
            // Verificar serviço 2FA
            String testSecret = twoFactorService.generateSecret();
            health.put("twoFactorService", testSecret != null ? "OK" : "ERROR");
            
            // Verificar sistema do supermercado
            long categoriaCount = categoriaRepository.count();
            long produtoCount = produtoRepository.count();
            long estoqueCount = estoqueRepository.count();
            long fornecedorCount = fornecedorRepository.count();
            
            health.put("supermercado", Map.of(
                "database", "OK",
                "categorias", categoriaCount,
                "produtos", produtoCount,
                "estoques", estoqueCount,
                "fornecedores", fornecedorCount
            ));
            
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
        
        instructions.put("titulo", "Instruções para Teste do Sistema Completo");
        
        instructions.put("sistemaLogin", List.of(
            "1. Acesse a interface web em http://localhost:8080",
            "2. Registre um novo usuário na aba 'Registro'",
            "3. Faça login com as credenciais criadas",
            "4. Configure o 2FA na área autenticada",
            "5. Escaneie o QR Code com Google Authenticator ou similar",
            "6. Teste o login com 2FA habilitado"
        ));
        
        instructions.put("sistemaSupermercado", List.of(
            "1. Acesse a documentação Swagger em http://localhost:8080/swagger-ui.html",
            "2. Faça login para obter o token JWT (/api/auth/login)",
            "3. Use o botão 'Authorize' no Swagger com 'Bearer {seu-token}'",
            "4. Teste os endpoints de categorias (/api/supermercado/categorias)",
            "5. Explore produtos e seus códigos de barras (/api/supermercado/produtos)",
            "6. Verifique controle de estoque (/api/supermercado/estoques)",
            "7. Consulte produtos com estoque baixo (/api/supermercado/estoques/baixo)",
            "8. Veja produtos em promoção (/api/supermercado/produtos/promocao)"
        ));
        
        instructions.put("dadosExemplo", Map.of(
            "observacao", "O sistema já possui dados de exemplo criados automaticamente",
            "conteudo", "9 produtos, 4 categorias, 2 fornecedores, estoques configurados",
            "alertas", "Alguns produtos têm estoque baixo e prazo de validade próximo"
        ));
        
        instructions.put("endpointsTeste", Map.of(
            "sistemaGeral", Map.of(
                "demo", "GET /api/test/demo - Informações completas do sistema",
                "stats", "GET /api/test/stats - Estatísticas detalhadas",
                "health", "GET /api/test/health - Status de todos os módulos"
            ),
            "demoSupermercado", Map.of(
                "supermercadoDemo", "GET /api/test/supermercado-demo - Demo do supermercado",
                "alertasDemo", "GET /api/test/alertas-demo - Alertas e relatórios",
                "totpDemo", "GET /api/test/totp-demo - Demonstração 2FA"
            )
        ));
        
        instructions.put("recursosDestaque", List.of(
            "✅ Sistema de login com 2FA totalmente funcional",
            "✅ API REST completa para gestão de supermercado",
            "✅ Documentação interativa com Swagger/OpenAPI",
            "✅ Controle de estoque em tempo real com alertas",
            "✅ Autenticação JWT integrada em todos os módulos",
            "✅ Dados de exemplo para teste imediato",
            "✅ Arquitetura escalável e bem estruturada"
        ));
        
        instructions.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(instructions);
    }
} 