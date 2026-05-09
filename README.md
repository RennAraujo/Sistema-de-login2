# Sistema de Login Seguro com 2FA

> ⚠️ Em refatoração para se tornar **iam-portfolio** — uma plataforma de Identity & Access Management (IAM) com SSO (OAuth2/OIDC + SAML2), ciclo de vida de identidades, SCIM 2.0, governança e assistente IA com RAG. Acompanhe o histórico de commits.

## 📋 Descrição

Sistema de autenticação desenvolvido com **Spring Boot** e interface em **HTML/CSS/JavaScript**, implementando **autenticação de duas etapas (2FA)** via **TOTP (Time-based One-Time Password)**.

## 🚀 Tecnologias Utilizadas

### Backend
- **Spring Boot 3.2.0** - Framework principal
- **Spring Security 6** - Segurança e autenticação
- **JWT (JSON Web Tokens)** - Gerenciamento de sessões
- **TOTP** - Autenticação de duas etapas
- **BCrypt** - Criptografia de senhas
- _(Postgres + Flyway substituirão o H2 in-memory na próxima fase do refactor)_
- **Maven** - Gerenciamento de dependências
- **Swagger/OpenAPI 3.0** - Documentação da API
- **Spring Data JPA** - Persistência de dados
- **Bean Validation** - Validação de dados

### Frontend
- **HTML5** - Estrutura
- **CSS3** - Estilização moderna e responsiva
- **JavaScript (Vanilla)** - Interatividade
- **Font Awesome** - Ícones

### Bibliotecas de Segurança
- **JJWT** - Manipulação de tokens JWT
- **TOTP Library** - Geração e validação de códigos TOTP
- **ZXing** - Geração de QR Codes

## 🛡️ Recursos de Segurança

### Autenticação Básica
- ✅ Registro de usuários com validação
- ✅ Login seguro com credenciais
- ✅ Criptografia de senhas com BCrypt
- ✅ Validação de tokens JWT
- ✅ Gerenciamento de sessões stateless

### Autenticação de Duas Etapas (2FA)
- ✅ Geração de códigos TOTP a cada 30 segundos
- ✅ QR Code para configuração em apps autenticadores
- ✅ Códigos de backup para recuperação
- ✅ Suporte a aplicativos como Google Authenticator, Microsoft Authenticator, Authy

### Medidas de Segurança Adicionais
- ✅ Proteção CORS configurada
- ✅ Validação de entrada rigorosa
- ✅ Headers de segurança
- ✅ Rate limiting implícito
- ✅ Sanitização de dados

## 📁 Estrutura do Projeto

```
Sistema de Login/
├── src/
│   ├── main/
│   │   ├── java/com/login/
│   │   │   ├── SistemaLoginSeguroApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── dto/
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   └── TwoFactorSetupResponse.java
│   │   │   ├── model/
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtUtil.java
│   │   │   └── service/
│   │   │       ├── AuthService.java
│   │   │       └── TwoFactorService.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/style.css
│   │       │   ├── js/app.js
│   │       │   └── index.html
│   │       └── application.properties
├── pom.xml
└── README.md
```

## 🔧 Como Executar

### Pré-requisitos
- **Java 17** ou superior
- **Maven 3.6** ou superior

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone <url-do-repositorio>
   cd Sistema\ de\ Login
   ```

2. **Compile e execute a aplicação:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **Acesse a aplicação:**
   - Interface Web: http://localhost:8080
   - **Documentação Swagger: http://localhost:8080/swagger-ui.html**

## 📱 Como Usar

### 1. Registro de Usuário
1. Acesse http://localhost:8080
2. Clique na aba "Registro"
3. Preencha os dados (nome, email, senha)
4. Clique em "Criar Conta"

### 2. Login Básico
1. Na aba "Login", digite suas credenciais
2. Clique em "Entrar"
3. Você será direcionado ao dashboard

### 3. Configurar 2FA
1. No dashboard, clique em "Ativar 2FA"
2. Escaneie o QR Code com seu app autenticador
3. Digite o código gerado para confirmar
4. Salve os códigos de backup em local seguro

### 4. Login com 2FA
1. Faça login normalmente
2. Quando solicitado, digite o código do seu app
3. Ou use um código de backup se necessário

## 📊 Endpoints de API

### Autenticação
- `POST /api/auth/register` - Registro de usuário
- `POST /api/auth/login` - Login
- `GET /api/auth/me` - Informações do usuário
- `GET /api/auth/validate` - Validar token
- `POST /api/auth/logout` - Logout

### 2FA
- `POST /api/auth/2fa/setup` - Configurar 2FA
- `POST /api/auth/2fa/confirm` - Confirmar 2FA
- `POST /api/auth/2fa/disable` - Desabilitar 2FA

## 🔐 Segurança Implementada

### Autenticação JWT
- Tokens assinados com chave secreta
- Expiração automática (24 horas)
- Verificação em todas as rotas protegidas

### Criptografia
- Senhas com BCrypt (salt rounds)
- Secrets 2FA criptografados
- Comunicação protegida

### Validações
- Entrada sanitizada
- Validação de email
- Senhas com critérios mínimos
- Rate limiting implícito

### CORS e Headers
- Origens permitidas configuráveis
- Headers de segurança
- Proteção CSRF desabilitada (API stateless)

## 🏗️ Arquitetura

### Padrões Utilizados
- **MVC (Model-View-Controller)**
- **Repository Pattern**
- **DTO (Data Transfer Object)**
- **Dependency Injection**
- **RESTful API**

### Separação de Responsabilidades
- **Controllers**: Endpoints e validação
- **Services**: Lógica de negócio
- **Repositories**: Acesso a dados
- **Security**: Autenticação e autorização
- **DTOs**: Transferência de dados

## 🎨 Interface

### Design
- Interface moderna e responsiva
- Tema claro com variáveis CSS
- Ícones Font Awesome
- Animações suaves
- Feedback visual para usuário

### Funcionalidades UI
- Alternância entre abas
- Mostrar/ocultar senhas
- Notificações toast
- Loading states
- Validação em tempo real

## 📈 Escalabilidade

### Considerações
- Arquitetura stateless
- Separação clara de responsabilidades
- Código modular e reutilizável
- Configurações externalizadas
- Logging estruturado

### Próximas Etapas (refatoração para iam-portfolio)
- PostgreSQL persistente via Docker + Flyway migrations
- Spring Authorization Server (OAuth2/OIDC)
- SAML2 IdP custom
- SCIM 2.0 inbound + conector Python (FastAPI) para provisionamento
- Auditoria imutável e RBAC com Roles/Permissions/Groups
- Ciclo de vida de identidades (joiner-mover-leaver)
- Assistente IAM com RAG (Claude API + pgvector)
- Testes com Testcontainers + GitHub Actions CI/CD

## 🧪 Testando o 2FA

### Apps Recomendados
- **Google Authenticator** (Android/iOS)
- **Microsoft Authenticator** (Android/iOS)
- **Authy** (Android/iOS/Desktop)
- **1Password** (Multiplataforma)

### Como Testar
1. Cadastre-se em `/api/auth/register`
2. Faça login em `/api/auth/login`
3. Ative o 2FA em `/api/auth/2fa/setup` e configure no seu app autenticador
4. Teste o fluxo completo na interface

## 📚 Documentação da API (Swagger)

### Acessando a Documentação
1. Inicie a aplicação
2. Acesse http://localhost:8080/swagger-ui.html
3. Explore todos os endpoints disponíveis
4. Teste as requisições diretamente pela interface

### Recursos do Swagger
- ✅ Documentação interativa completa
- ✅ Modelos de request/response
- ✅ Teste de endpoints com autenticação JWT
- ✅ Download da especificação OpenAPI
- ✅ Agrupamento por tags (Auth, 2FA)

### Autenticação no Swagger
1. Faça login através do endpoint `/api/auth/login`
2. Copie o token JWT retornado
3. Clique no botão "Authorize" no topo da página
4. Digite: `Bearer {seu-token-aqui}`
5. Agora você pode testar todos os endpoints protegidos

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique os logs da aplicação
2. Consulte a documentação interativa em `/swagger-ui.html`

## 📄 Licença

Este projeto foi desenvolvido para fins de demonstração e avaliação técnica.

---

**Desenvolvido com ❤️ usando Spring Boot + 2FA** 