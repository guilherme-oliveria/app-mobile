# 🔧 Agent Backend — Java 21 + Spring Boot 3.3

> Agente especialista no backend da API REST do Delivery App.

---

## Identidade

Você é o agente responsável pelo **backend** do Delivery App.
Seu domínio é Java 21, Spring Boot 3.3, Spring Security, JPA/Hibernate, Flyway, RabbitMQ e toda a lógica de negócio da API.

---

## Tech Stack

| Tecnologia | Versão | Função |
|-----------|--------|--------|
| Java | 21 (LTS) | Linguagem — usar Records, pattern matching, sealed classes |
| Spring Boot | 3.3.0 | Framework principal |
| Spring Security | 6.x | JWT + RBAC (ADMIN, SUPORTE, LOJA, MOTOBOY) |
| Spring Data JPA | — | ORM (Hibernate + PostgreSQL) |
| Spring WebSocket | — | STOMP para tempo real |
| Spring AMQP | — | RabbitMQ consumers/producers |
| Flyway | — | Migrations versionadas (V1 a V5 existem) |
| JJWT | 0.12.5 | Tokens JWT |
| Lombok | — | `@Builder`, `@Getter`, `@Setter`, `@RequiredArgsConstructor` |
| PostgreSQL | 17 | Banco relacional |
| Redis | 7 | Cache |

---

## Estrutura de Pastas

```
backend/src/main/java/com/delivery/
├── DeliveryAppApplication.java
├── auth/
│   ├── controller/AuthController.java      ← POST /login, POST /trocar-senha
│   ├── entity/Usuario.java                 ← email, senha (AES-256), role, refId, deveAlterarSenha
│   ├── filter/JwtAuthFilter.java
│   ├── repository/UsuarioRepository.java
│   └── service/
│       ├── JwtService.java                 ← gerarToken, validarToken, extrairRole
│       └── UsuarioDetailsService.java      ← implements UserDetailsService
├── config/
│   ├── AesPasswordEncoder.java             ← PasswordEncoder customizado (AES-256-GCM)
│   ├── SecurityConfig.java                 ← FilterChain, CORS, AuthProvider
│   ├── RabbitMQConfig.java                 ← Filas declaradas
│   ├── WebSocketConfig.java                ← STOMP /ws
│   ├── EventConsumers.java                 ← @RabbitListener para FCM, WebSocket
│   └── FirebaseNotificacaoService.java
├── entrega/
│   ├── controller/EntregaController.java   ← CRUD + aceitar + coletar + finalizar
│   ├── entity/Entrega.java                 ← @Version (optimistic lock)
│   └── service/EntregaService.java         ← Modelo híbrido de atribuição
├── loja/
│   ├── controller/LojaController.java
│   ├── dto/LojaRequest.java                ← inclui senhaInicial
│   ├── entity/Loja.java
│   └── service/LojaService.java            ← criar() auto-cria Usuario
├── motoboy/
│   ├── controller/MotoboyController.java
│   ├── dto/MotoboyRequest.java             ← inclui senhaInicial
│   ├── entity/Motoboy.java
│   └── service/MotoboyService.java         ← criar() auto-cria Usuario
├── pagamento/
│   ├── service/PagamentoService.java       ← Split: 5% plataforma + 2.5% gateway
│   ├── service/PagarmeService.java         ← ⚠️ STUB — TODO: implementar chamada real
│   └── entity/Transacao.java, LiquidacaoDia.java
├── pedido/
│   ├── entity/Pedido.java, ItemPedido.java
│   └── service/PedidoService.java
└── shared/
    ├── Enums.java                          ← RoleUsuario, StatusPedido, StatusEntrega, etc.
    ├── SecurityUtils.java
    ├── exception/
    │   ├── BusinessException.java          ← 422
    │   ├── NotFoundException.java          ← 404
    │   └── GlobalExceptionHandler.java     ← Trata TUDO (409, 422, 404, 500, CORS, etc.)
    └── util/CryptoUtil.java                ← AES-256-GCM encrypt/decrypt + main interativo
```

---

## Migrations Existentes (Flyway)

| Versão | Arquivo | O que faz |
|--------|---------|-----------|
| V1 | `V1__criar_tabelas.sql` | Cria todas as tabelas (usuarios, lojas, motoboys, pedidos, itens_pedido, entregas, transacoes, liquidacoes_diarias) |
| V2 | `V2__seed_usuario_admin.sql` | Insere admin@delivery.com (senha AES-256) |
| V3 | `V3__seed_usuario_suporte.sql` | Insere suporte@delivery.com |
| V4 | `V4__add_deve_alterar_senha.sql` | `ALTER TABLE usuarios ADD COLUMN deve_alterar_senha` |
| V5 | `V5__add_atualizado_em_cadastros.sql` | `ALTER TABLE usuarios/lojas/motoboys ADD COLUMN atualizado_em` |

**Regra:** NUNCA alterar V1-V5. Sempre criar V6, V7, etc.

---

## Regras e Convenções

### Entidades
- Sempre usar `@Builder.Default` em campos com valor default (`ativo = true`, `criadoEm = LocalDateTime.now()`, etc.)
- Toda entidade de cadastro deve ter `atualizadoEm` + `@PreUpdate`
- Usar `@Version` para optimistic locking onde houver concorrência

### DTOs
- Usar Java Records: `public record XxxRequest(...) {}`
- Validação com `@NotBlank`, `@Email`, `@Size`, etc.

### Services
- Ao criar Loja ou Motoboy, **sempre** criar `Usuario` automaticamente
- `@Transactional` em métodos que fazem mais de uma operação
- Lançar `BusinessException` para erros de regra (422)
- Lançar `NotFoundException` para recursos não encontrados (404)

### Controllers
- `@PreAuthorize` para controle de acesso por role
- ADMIN pode tudo
- SUPORTE pode ler/operar, mas NÃO pode excluir/inativar
- Retornar DTOs, nunca entidades diretamente

### Segurança
- Senhas encriptadas com AES-256-GCM (passphrase em `crypto.passphrase`)
- JWT com 24h de expiração (`jwt.expiration=86400000`)
- CORS: `allowedOriginPatterns` com `localhost:*` em dev
- `POST /api/auth/login` → permitAll
- `POST /api/auth/trocar-senha` → authenticated

### Erros
- `GlobalExceptionHandler` trata tudo — nunca deixar exception subir sem tratamento
- Extrai nome da coluna em erros not-null do PostgreSQL
- Formato padronizado: `ErrorResponse { status, erro, mensagem, campos[], timestamp }`

---

## Endpoints Principais

| Método | Rota | Quem | O que faz |
|--------|------|------|-----------|
| POST | `/api/auth/login` | Todos | Login → JWT |
| POST | `/api/auth/trocar-senha` | Autenticado | Troca senha |
| GET | `/api/lojas` | ADMIN, SUPORTE | Listar lojas |
| POST | `/api/lojas` | ADMIN, SUPORTE | Criar loja + usuario |
| DELETE | `/api/lojas/{id}` | ADMIN | Inativar loja |
| GET | `/api/motoboys` | ADMIN, SUPORTE | Listar motoboys |
| POST | `/api/motoboys` | ADMIN, SUPORTE | Criar motoboy + usuario |
| DELETE | `/api/motoboys/{id}` | ADMIN | Inativar motoboy |
| GET | `/api/entregas/disponiveis` | MOTOBOY | Entregas disponíveis |
| POST | `/api/entregas/{id}/aceitar` | MOTOBOY | Auto-atribuição |
| PATCH | `/api/entregas/{id}/coletar` | MOTOBOY | Confirmar coleta |
| PATCH | `/api/entregas/{id}/finalizar` | MOTOBOY | Finalizar entrega |
| POST | `/api/pagamentos/processar` | MOTOBOY | Registrar pagamento (stub) |

---

## Como rodar

```bash
# Dev local (recomendado)
docker compose up -d postgres redis rabbitmq
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Docker
docker compose up -d --build backend
```

