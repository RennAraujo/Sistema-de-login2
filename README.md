# Sistema de Login Seguro com 2FA e Gestão de Supermercado

## 📋 Descrição

Sistema completo de autenticação desenvolvido com **Spring Boot** e interface moderna em **HTML/CSS/JavaScript**, implementando **autenticação de duas etapas (2FA)** usando **TOTP (Time-based One-Time Password)**, integrado com um **sistema de gestão de supermercado** com controle de produtos, categorias, estoque e fornecedores.

## 🚀 Tecnologias Utilizadas

### Backend
- **Spring Boot 3.2.0** - Framework principal
- **Spring Security 6** - Segurança e autenticação
- **JWT (JSON Web Tokens)** - Gerenciamento de sessões
- **TOTP** - Autenticação de duas etapas
- **H2 Database** - Banco de dados em memória
- **BCrypt** - Criptografia de senhas
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

## 🛒 Sistema de Supermercado

### Funcionalidades
- ✅ Gestão completa de produtos
- ✅ Categorização de produtos
- ✅ Controle de estoque em tempo real
- ✅ Gestão de fornecedores
- ✅ Alertas de estoque baixo
- ✅ Controle de validade de produtos
- ✅ Produtos em promoção
- ✅ Busca avançada por vários critérios

### Entidades do Sistema
1. **Produtos**
   - Informações completas do produto
   - Código de barras único
   - Preços normal e promocional
   - Imagem do produto
   - Relação com categoria e fornecedores

2. **Categorias**
   - Organização hierárquica
   - Status ativo/inativo
   - Descrição detalhada

3. **Estoque**
   - Quantidade atual, mínima e máxima
   - Localização no armazém
   - Controle de lote e validade
   - Histórico de movimentações

4. **Fornecedores**
   - Cadastro completo com CNPJ
   - Dados de contato
   - Produtos fornecidos
   - Status ativo/inativo

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
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── TestController.java
│   │   │   │   └── supermarket/
│   │   │   │       ├── CategoriaController.java
│   │   │   │       ├── ProdutoController.java
│   │   │   │       └── EstoqueController.java
│   │   │   ├── dto/
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── TwoFactorSetupResponse.java
│   │   │   │   └── supermarket/
│   │   │   │       ├── CategoriaDto.java
│   │   │   │       ├── ProdutoDto.java
│   │   │   │       └── EstoqueDto.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   └── supermarket/
│   │   │   │       ├── Categoria.java
│   │   │   │       ├── Produto.java
│   │   │   │       ├── Estoque.java
│   │   │   │       └── Fornecedor.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── supermarket/
│   │   │   │       ├── CategoriaRepository.java
│   │   │   │       ├── ProdutoRepository.java
│   │   │   │       ├── EstoqueRepository.java
│   │   │   │       └── FornecedorRepository.java
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtUtil.java
│   │   │   └── service/
│   │   │       ├── AuthService.java
│   │   │       ├── TwoFactorService.java
│   │   │       └── supermarket/
│   │   │           ├── CategoriaService.java
│   │   │           ├── ProdutoService.java
│   │   │           └── EstoqueService.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── style.css
│   │       │   ├── js/
│   │       │   │   ├── app.js
│   │       │   │   └── test.js
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
   - Console H2: http://localhost:8080/h2-console
   - API de Demonstração: http://localhost:8080/api/test/instructions
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

## 📊 Demonstração para Recrutadores

### Botão de Teste
A aplicação possui um **botão "Teste para Recrutador"** que demonstra:

1. **Informações do Sistema**
   - Tecnologias utilizadas
   - Funcionalidades implementadas
   - Endpoints disponíveis

2. **Demo 2FA**
   - Geração de códigos TOTP em tempo real
   - Verificação de códigos
   - Explicação do funcionamento

3. **Estatísticas**
   - Número de usuários
   - Taxa de adoção do 2FA
   - Métricas do sistema

### Endpoints de API

#### Autenticação
- `POST /api/auth/register` - Registro de usuário
- `POST /api/auth/login` - Login
- `GET /api/auth/me` - Informações do usuário
- `GET /api/auth/validate` - Validar token
- `POST /api/auth/logout` - Logout

#### 2FA
- `POST /api/auth/2fa/setup` - Configurar 2FA
- `POST /api/auth/2fa/confirm` - Confirmar 2FA
- `POST /api/auth/2fa/disable` - Desabilitar 2FA

#### Demonstração
- `GET /api/test/demo` - Informações do sistema
- `GET /api/test/totp-demo` - Demo TOTP
- `GET /api/test/stats` - Estatísticas
- `POST /api/test/verify-totp` - Verificar código TOTP

#### Supermercado - Categorias
- `GET /api/supermercado/categorias` - Listar todas as categorias
- `GET /api/supermercado/categorias/{id}` - Buscar categoria por ID
- `GET /api/supermercado/categorias/ativas` - Listar categorias ativas
- `GET /api/supermercado/categorias/buscar?nome=` - Buscar por nome
- `POST /api/supermercado/categorias` - Criar categoria
- `PUT /api/supermercado/categorias/{id}` - Atualizar categoria
- `DELETE /api/supermercado/categorias/{id}` - Deletar categoria
- `PATCH /api/supermercado/categorias/{id}/desativar` - Desativar categoria

#### Supermercado - Produtos
- `GET /api/supermercado/produtos` - Listar todos os produtos
- `GET /api/supermercado/produtos/{id}` - Buscar produto por ID
- `GET /api/supermercado/produtos/codigo-barras/{codigo}` - Buscar por código de barras
- `GET /api/supermercado/produtos/ativos` - Listar produtos ativos
- `GET /api/supermercado/produtos/buscar?nome=` - Buscar por nome
- `GET /api/supermercado/produtos/categoria/{id}` - Buscar por categoria
- `GET /api/supermercado/produtos/preco?precoMin=&precoMax=` - Buscar por faixa de preço
- `GET /api/supermercado/produtos/promocao` - Produtos em promoção
- `POST /api/supermercado/produtos` - Criar produto
- `PUT /api/supermercado/produtos/{id}` - Atualizar produto
- `DELETE /api/supermercado/produtos/{id}` - Deletar produto
- `PATCH /api/supermercado/produtos/{id}/desativar` - Desativar produto

#### Supermercado - Estoque
- `GET /api/supermercado/estoques` - Listar todos os estoques
- `GET /api/supermercado/estoques/{id}` - Buscar estoque por ID
- `GET /api/supermercado/estoques/produto/{id}` - Buscar estoque por produto
- `GET /api/supermercado/estoques/baixo` - Produtos com estoque baixo
- `GET /api/supermercado/estoques/excedido` - Produtos com estoque excedido
- `GET /api/supermercado/estoques/vencidos` - Produtos vencidos
- `GET /api/supermercado/estoques/localizacao?localizacao=` - Buscar por localização
- `POST /api/supermercado/estoques` - Criar registro de estoque
- `PUT /api/supermercado/estoques/{id}` - Atualizar estoque
- `PATCH /api/supermercado/estoques/produto/{id}/adicionar?quantidade=` - Adicionar ao estoque
- `PATCH /api/supermercado/estoques/produto/{id}/remover?quantidade=` - Remover do estoque
- `DELETE /api/supermercado/estoques/{id}` - Deletar registro de estoque

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

### Melhorias Futuras
- Banco de dados persistente (PostgreSQL/MySQL)
- Redis para cache de sessões e produtos
- Rate limiting avançado
- Logs centralizados
- Monitoramento com métricas
- Testes automatizados
- Sistema de vendas e PDV
- Relatórios gerenciais
- Integração com sistemas fiscais
- Dashboard administrativo para o supermercado
- Aplicativo mobile para conferência de estoque
- Sistema de compras automatizado
- Alertas por email/SMS para estoque baixo

## 🧪 Testando o 2FA

### Apps Recomendados
- **Google Authenticator** (Android/iOS)
- **Microsoft Authenticator** (Android/iOS)
- **Authy** (Android/iOS/Desktop)
- **1Password** (Multiplataforma)

### Como Testar
1. Use o endpoint `/api/test/totp-demo` para gerar um secret
2. Configure no seu app autenticador
3. Use `/api/test/verify-totp` para validar códigos
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
- ✅ Agrupamento por tags (Auth, 2FA, Categorias, Produtos, Estoque)

### Autenticação no Swagger
1. Faça login através do endpoint `/api/auth/login`
2. Copie o token JWT retornado
3. Clique no botão "Authorize" no topo da página
4. Digite: `Bearer {seu-token-aqui}`
5. Agora você pode testar todos os endpoints protegidos

## 💡 Exemplo de Uso - Sistema Supermercado

### 1. Criar uma Categoria
```json
POST /api/supermercado/categorias
{
  "nome": "Alimentos",
  "descricao": "Produtos alimentícios em geral",
  "ativa": true
}
```

### 2. Criar um Produto
```json
POST /api/supermercado/produtos
{
  "nome": "Arroz Integral 5kg",
  "descricao": "Arroz integral tipo 1",
  "codigoBarras": "7891234567890",
  "preco": 25.90,
  "precoPromocional": 22.90,
  "unidadeMedida": "kg",
  "categoriaId": 1,
  "ativo": true
}
```

### 3. Adicionar ao Estoque
```json
PATCH /api/supermercado/estoques/produto/1/adicionar?quantidade=100
```

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique os logs da aplicação
2. Acesse `/api/test/health` para status do sistema
3. Consulte a documentação dos endpoints em `/api/test/instructions`

## 📄 Licença

Este projeto foi desenvolvido para fins de demonstração e avaliação técnica.

---

**Desenvolvido com ❤️ usando Spring Boot + 2FA** 