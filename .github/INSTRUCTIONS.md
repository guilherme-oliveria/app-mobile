# 📘 INSTRUCTIONS — Delivery App (Smart POS)

> Documento completo de referência do projeto. Contém arquitetura, setup,
> regras de negócio, decisões técnicas e jurídicas.
>
> **Atualizado em:** Abril 2026

---

## 📋 Índice

1. [Visão Geral](#1--visão-geral)
2. [Arquitetura](#2--arquitetura)
3. [Tech Stack Completa](#3--tech-stack-completa)
4. [Estrutura de Pastas](#4--estrutura-de-pastas)
5. [Pré-requisitos — O que instalar](#5--pré-requisitos)
6. [Docker e WSL no Windows](#6--docker-e-wsl-no-windows)
7. [Setup Passo a Passo](#7--setup-passo-a-passo)
8. [Desenvolvimento no Dia a Dia](#8--desenvolvimento-no-dia-a-dia)
9. [Docker — Build de Produção / CI](#9--docker--build-de-produção--ci)
10. [Configurações e Variáveis](#10--configurações-e-variáveis)
11. [Endpoints da API](#11--endpoints-da-api)
12. [Fluxo de Telas](#12--fluxo-de-telas)
13. [Entidades do Banco](#13--entidades-do-banco)
14. [Modelo de Negócio e Jurídico](#14--modelo-de-negócio-e-jurídico)
15. [Troubleshooting](#15--troubleshooting)
16. [Próximos Passos](#16--próximos-passos)

---

## 1 — Visão Geral

Plataforma de entrega com maquininha smart. Cada motoboy carrega uma maquininha de cartão,
busca a mercadoria na loja, cobra o cliente na entrega, e o sistema faz split de pagamento
automaticamente via Pagar.me/Zoop — **a plataforma nunca toca no dinheiro da loja**.

### Apps do projeto

| App | Tecnologia | Quem usa | O que faz |
|-----|-----------|----------|-----------|
| **Backend** | Java 21 + Spring Boot 3.3 | — | API REST central (todos os apps consomem a mesma API) |
| **Admin** | Angular 21 + Material | Administrador | Painel web: dashboard, CRUD lojas/motoboys, relatórios, atribuição de entregas |
| **Motoboy App** | Flutter 3.29 | Motoboy | App Android para Smart POS: ver entregas, confirmar coleta, cobrar na maquininha |
| **Store App** | Flutter 3.29 | Lojista | App Android/iOS: acompanhar pedidos, criar novos pedidos |

---

## 2 — Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTENDS                            │
│                                                             │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐   │
│  │ Admin Angular │  │  Motoboy App  │  │   Store App   │   │
│  │  :4200 (web)  │  │   (Flutter)   │  │   (Flutter)   │   │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘   │
│          │ HTTP/WS          │ HTTP/WS          │ HTTP       │
└──────────┼──────────────────┼──────────────────┼────────────┘
           │                  │                  │
           ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│               BACKEND — Spring Boot :8080                   │
│                                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐   │
│  │   Auth   │ │  Pedido  │ │ Entrega  │ │  Pagamento   │   │
│  │  (JWT)   │ │  Domain  │ │  Domain  │ │  Split/Liq.  │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────┘   │
│  ┌──────────────────┐ ┌────────────┐ ┌──────────────────┐   │
│  │ Spring Security  │ │  WebSocket │ │     Flyway       │   │
│  └──────────────────┘ └────────────┘ └──────────────────┘   │
└────────┬──────────────────┬──────────────────┬──────────────┘
         │                  │                  │
         ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  PostgreSQL  │   │    Redis     │   │   RabbitMQ   │
│    :5432     │   │    :6379     │   │  :5672/15672 │
│   (dados)    │   │   (cache)    │   │   (filas)    │
└──────────────┘   └──────────────┘   └──────────────┘
```

Todos os frontends (Angular + 2 Flutter) consomem a **mesma API REST**.
WebSocket (STOMP) é usado pelo Admin e Motoboy App para atualizações em tempo real.
RabbitMQ gerencia filas de entrega, notificação e pagamento.
Firebase FCM envia push notifications para os apps Flutter.

---

## 3 — Tech Stack Completa

### Backend

| Tecnologia | Versão | Função |
|-----------|--------|--------|
| Java | 21 (LTS) | Linguagem |
| Spring Boot | 3.3.0 | Framework |
| Spring Security | 6.x | Auth + autorização |
| Spring Data JPA | — | ORM (Hibernate) |
| Spring WebSocket | — | Tempo real (STOMP) |
| Spring AMQP | — | Mensageria (RabbitMQ) |
| Spring Data Redis | — | Cache |
| Flyway | — | Migrations de banco |
| JJWT | 0.12.5 | Tokens JWT |
| Lombok | — | Reduzir boilerplate |
| MapStruct | 1.5.5 | DTO ↔ Entity |
| Firebase Admin | 9.3.0 | Push notifications |
| PostgreSQL Driver | runtime | Conexão com banco |

### Admin (Frontend Web)

| Tecnologia | Versão | Função |
|-----------|--------|--------|
| Angular | 21 | Framework SPA |
| Angular Material | 21 | UI components |
| TypeScript | 5.9 | Linguagem |
| RxStomp | 2.x | WebSocket STOMP client |
| RxJS | 7.8 | Reatividade |

### Mobile (Flutter)

| Tecnologia | Versão | Usado em | Função |
|-----------|--------|----------|--------|
| Flutter | 3.29.3 | Ambos | Framework |
| Dart | >=3.3 | Ambos | Linguagem |
| Provider | 6.1 | Ambos | State management |
| Dio | 5.4 | motoboy_app | HTTP client |
| http | 1.2 | Ambos | HTTP client |
| Geolocator | 11.0 | motoboy_app | GPS do motoboy |
| Firebase Messaging | 14.7 | Ambos | Push notifications |
| Flutter Secure Storage | 9.0 | Ambos | Armazena JWT |
| STOMP Dart Client | 0.4 | motoboy_app | WebSocket |
| Intl | 0.19 | Ambos | Formatação de datas/moedas |

### Infraestrutura (Docker)

| Serviço | Imagem | Porta | Função |
|---------|--------|-------|--------|
| PostgreSQL | `postgres:17-alpine` | 5432 | Banco principal |
| Redis | `redis:7-alpine` | 6379 | Cache |
| RabbitMQ | `rabbitmq:3-management-alpine` | 5672 / 15672 | Filas + painel web |

---

## 4 — Estrutura de Pastas

```
app-mobile/
├── .github/
│   ├── INSTRUCTIONS.md          ← ESTE ARQUIVO
│   └── instrucaoAPP.txt         ← Decisões iniciais do projeto
│
├── docker-compose.yml           ← Orquestra tudo (infra + builds)
├── README.md                    ← Guia rápido de setup
│
├── backend/                     ← Java 21 + Spring Boot 3.3
│   ├── Dockerfile               ← Multi-stage: Maven build → JRE runtime
│   ├── pom.xml                  ← Dependências Maven
│   └── src/main/
│       ├── java/com/delivery/   ← Código Java (auth, pedido, entrega, etc.)
│       └── resources/
│           ├── application.properties        ← Config principal (produção)
│           ├── application-local.properties  ← Config dev (ddl-auto=update)
│           └── db/migration/                 ← Scripts Flyway (V1__*.sql)
│
├── admin/
│   └── admin-angular/           ← Angular 21
│       ├── Dockerfile            ← Multi-stage: Node build → Nginx serve
│       ├── nginx.conf            ← Config Nginx para SPA (redireciona rotas)
│       ├── package.json
│       └── src/
│           ├── app/
│           │   ├── core/         ← Guards, interceptors, services singleton
│           │   ├── modules/      ← Telas: auth, dashboard, pedidos, motoboys, lojas, relatorios
│           │   └── shared/       ← Models, componentes compartilhados
│           └── environments/
│               ├── environment.ts        ← Dev (localhost:8080)
│               └── environment.prod.ts   ← Produção
│
├── mobile/
│   ├── Dockerfile                ← Gera APKs dos 2 apps (flutter build apk)
│   ├── apk-output/               ← APKs gerados ficam aqui
│   │
│   ├── motoboy_app/              ← Flutter — App do Motoboy (Smart POS)
│   │   ├── pubspec.yaml
│   │   └── lib/
│   │       ├── main.dart
│   │       ├── models/           ← Classes de dados (Entrega, Pedido, etc.)
│   │       ├── screens/          ← login, home, entrega_ativa
│   │       ├── services/         ← auth_service, entrega_service
│   │       └── widgets/          ← Componentes reutilizáveis
│   │
│   └── store_app/                ← Flutter — App da Loja
│       ├── pubspec.yaml
│       └── lib/
│           ├── main.dart
│           ├── screens/          ← login, pedidos
│           └── services/         ← auth_service, pedido_service
```

---

## 5 — Pré-requisitos

### O que instalar na máquina

| Ferramenta | Versão mín. | Download | Verificar |
|-----------|-------------|----------|-----------|
| **Docker Desktop** | 4.x | https://docker.com/products/docker-desktop | `docker --version` |
| **Java JDK** | 21 | https://adoptium.net (Eclipse Temurin 21) | `java -version` |
| **Maven** | 3.9+ | https://maven.apache.org/download.cgi | `mvn -version` |
| **Node.js** | 22+ | https://nodejs.org (LTS) | `node -v` |
| **npm** | 10+ | Vem com Node.js | `npm -v` |
| **Flutter SDK** | 3.29+ | https://docs.flutter.dev/get-started/install/windows | `flutter --version` |
| **Android SDK** | API 34+ | Via Android Studio | `flutter doctor` |

### IDEs recomendadas

| Parte | IDE | Plugins |
|-------|-----|---------|
| Backend (Java) | **IntelliJ IDEA** | Lombok (já vem) |
| Admin (Angular) | **VS Code** ou IntelliJ | Angular Language Service |
| Mobile (Flutter) | **IntelliJ** ou Android Studio | Flutter + Dart |

> Pode usar IntelliJ para tudo se preferir IDE única.

---

## 6 — Docker e WSL no Windows

### Como o Docker funciona no Windows

O Docker Desktop **precisa do WSL 2** como backend, mas **instala tudo automaticamente**:

```
Docker Desktop (seu Windows)
    └── WSL 2 (camada Linux — automática, invisível)
        └── Containers (PostgreSQL, Redis, RabbitMQ, etc.)
```

**Você NÃO precisa:**
- Instalar WSL manualmente
- Instalar Ubuntu ou outra distro
- Abrir terminal Linux

**Verificar se WSL está ativo:**
```powershell
wsl --list --verbose
# Deve mostrar docker-desktop e docker-desktop-data com VERSION 2
```

### Virtualização na BIOS (obrigatório para WSL 2)

O WSL 2 requer virtualização por hardware. Verificar sem reiniciar:
```powershell
systeminfo | Select-String "Hyper-V"
```

| O que olhar | OK | Problema |
|-------------|-----|---------|
| `Virtualization Enabled In Firmware` | `Yes` ✅ | `No` → ativar na BIOS |
| `VM Monitor Mode Extensions` | `Yes` ✅ | `No` → CPU não suporta (raro) |

**Se precisar ativar na BIOS:**

| Fabricante | Tecla | Onde fica |
|-----------|-------|----------|
| Dell | F2 | Advanced → Virtualization |
| HP | F10 / Esc | Advanced → System Options |
| Lenovo | F1 / F2 | Security → Virtualization |
| ASUS | Del / F2 | Advanced → CPU Configuration |
| Acer | F2 / Del | Advanced → CPU Configuration |

Opção: **Intel VT-x** (Intel) ou **SVM Mode** (AMD) → `Enabled` → F10 para salvar.

### Docker Desktop — Confirmar configuração

Settings → General → ☑ **Use the WSL 2 based engine** (deve estar marcado).

### Regra de ouro: O que roda onde

| O que | Onde | Por quê |
|-------|------|---------|
| PostgreSQL, Redis, RabbitMQ | 🐳 **Docker** (sempre) | Isolado, sem instalar no Windows |
| Backend (Java/Spring Boot) | 💻 **Máquina local** (dev) | Mais rápido, debug, hot reload |
| Admin (Angular) | 💻 **Máquina local** (dev) | Hot reload com `ng serve` |
| Flutter apps | 💻 **Máquina local** (dev) | Precisa de emulador/device |
| Build de produção / CI | 🐳 **Docker** | Ambiente reprodutível |

### Duas formas de build — NÃO misture

| | Opção 1: Local (dev) | Opção 2: Docker (prod/CI) |
|---|---|---|
| **Usa** | Java, Maven, Node do **Windows** | Java, Maven, Node do **container** |
| **Backend** | `mvn spring-boot:run` | `docker compose up backend` |
| **Admin** | `ng serve` | `docker compose up admin` |
| **Flutter** | `flutter run` | `docker compose run --rm flutter-builder` |
| **Quando** | Dia a dia, desenvolvimento | Deploy, CI/CD, gerar artefatos |

**Nunca** rode `mvn` da sua máquina para gerar algo que vai para um container, ou vice-versa.
Escolha um caminho e siga ele.

---

## 7 — Setup Passo a Passo

### Passo 1 — Clonar o repositório

```powershell
cd C:\Projetos
git clone <url-do-repo> app-mobile
cd app-mobile
```

### Passo 2 — Subir infraestrutura (Docker)

```powershell
docker compose up -d postgres redis rabbitmq
```

Verificar:
```powershell
docker ps
# Deve mostrar 3 containers: delivery_postgres, delivery_redis, delivery_rabbitmq
```

| Serviço | Acesso | Credenciais |
|---------|--------|-------------|
| PostgreSQL | `localhost:5432` | user: `postgres`, pass: `postgres`, db: `delivery_db` |
| Redis | `localhost:6379` | (sem senha) |
| RabbitMQ | `localhost:5672` | user: `guest`, pass: `guest` |
| RabbitMQ UI | http://localhost:15672 | user: `guest`, pass: `guest` |

### Passo 3 — Backend

```powershell
cd C:\Projetos\app-mobile\backend

# Compilar
mvn clean install

# Rodar com perfil local (ddl-auto=update, logs detalhados)
mvn spring-boot:run -Dspring-boot.run.profiles=local
# API em http://localhost:8080
```

### Passo 4 — Criar usuário admin no banco

Conectar no PostgreSQL (DBeaver, IntelliJ Database, ou pgAdmin):
```
Host: localhost | Porta: 5432 | DB: delivery_db | User: postgres | Pass: postgres
```

```sql
-- Senha: admin123 (hash BCrypt)
INSERT INTO usuarios (email, senha, nome, role, ativo, criado_em)
VALUES (
  'admin@delivery.com',
  '$2a$10$N.zmdr9zkzoGtM7gKSomDOGHPibj.hAS5dMZRSBjbcFECNqDgr2Gy',
  'Administrador',
  'ADMIN',
  true,
  NOW()
);
```

### Passo 5 — Admin Angular

```powershell
cd C:\Projetos\app-mobile\admin\admin-angular
npm install
npm start
# Painel em http://localhost:4200
# Login: admin@delivery.com / admin123
```

### Passo 6 — Flutter (primeira vez)

```powershell
# Verificar ambiente
flutter doctor

# Aceitar licenças Android (se nunca fez)
flutter doctor --android-licenses
# Responda "y" para tudo

# Gerar estrutura Android dos projetos (NÃO sobrescreve código existente)
cd C:\Projetos\app-mobile\mobile\motoboy_app
flutter create --project-name motoboy_app --org com.delivery .
flutter pub get

cd C:\Projetos\app-mobile\mobile\store_app
flutter create --project-name store_app --org com.delivery .
flutter pub get
```

### Passo 7 — Configurar emulador Android

**Opção A — Emulador (recomendado):**
1. Android Studio → More Actions → Virtual Device Manager
2. Create Device → Pixel 7 → Next → API 34 → Finish
3. Clique ▶ para iniciar

**Opção B — Device físico:**
1. Ativar Opções de Desenvolvedor (tocar 7x em "Número da compilação")
2. Ativar Depuração USB
3. Conectar USB → aceitar prompt

```powershell
flutter devices   # Listar devices disponíveis
```

### Passo 8 — Rodar Flutter apps

```powershell
# Terminal 1 — Motoboy App
cd C:\Projetos\app-mobile\mobile\motoboy_app
flutter run

# Terminal 2 — Store App
cd C:\Projetos\app-mobile\mobile\store_app
flutter run
```

### Passo 9 — Firebase (opcional — push notifications)

1. https://console.firebase.google.com → criar projeto `delivery-app`
2. Adicionar app Android: package `com.delivery.motoboy_app`
3. Baixar `google-services.json` → colocar em:
   - `mobile/motoboy_app/android/app/google-services.json`
   - `mobile/store_app/android/app/google-services.json`
4. Backend: Firebase Console → Configurações → Contas de serviço → Gerar chave
   - Salvar como `backend/src/main/resources/firebase-credentials.json`

> Sem Firebase, os apps funcionam normalmente — só não enviam/recebem push.

---

## 8 — Desenvolvimento no Dia a Dia

### Fluxo típico de trabalho

```powershell
# 1. Subir infra (se não estiver rodando)
cd C:\Projetos\app-mobile
docker compose up -d postgres redis rabbitmq

# 2. Backend (Terminal 1)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. Admin Angular (Terminal 2)
cd admin\admin-angular
ng serve

# 4. Flutter — conforme necessidade (Terminal 3)
cd mobile\motoboy_app
flutter run
```

### Comandos — Backend

```powershell
mvn clean install                                      # Compilar + testes
mvn spring-boot:run                                    # Rodar (perfil default)
mvn spring-boot:run -Dspring-boot.run.profiles=local   # Rodar (perfil local)
mvn test                                               # Só testes
```

### Comandos — Angular

```powershell
npm start                                      # = ng serve → localhost:4200
ng generate component modules/nome-modulo      # Criar componente
ng build --configuration production             # Build de prod
```

### Comandos — Flutter

```powershell
flutter pub get          # Baixar dependências (= npm install / mvn install)
flutter run              # Rodar no emulador/device conectado
flutter run -d chrome    # Rodar no navegador (debug rápido, sem emulador)
flutter build apk        # Gerar APK release
flutter clean            # Limpar cache (quando dá erro estranho)
flutter doctor           # Diagnóstico do ambiente
```

**Hot Reload (Flutter):** com o app rodando, pressione:
- `r` → Hot Reload (atualiza sem perder estado — muito rápido)
- `R` → Hot Restart (reinicia o app completamente)

### Perfis do backend

| Perfil | Ativação | ddl-auto | Flyway | Uso |
|--------|---------|----------|--------|-----|
| **(default)** | sem flag | `validate` | ativo | Produção / CI |
| **local** | `-Dspring-boot.run.profiles=local` | `update` | ativo | Desenvolvimento |

No perfil `local`, o Hibernate cria/atualiza tabelas automaticamente (`update`).
No perfil default, o Flyway roda as migrations e o Hibernate apenas valida (`validate`).

---

## 9 — Docker — Build de Produção / CI

### Subir TUDO via Docker

```powershell
docker compose up -d --build
# Sobe: postgres, redis, rabbitmq, backend, admin
# Backend: http://localhost:8080
# Admin:   http://localhost:4200 (via Nginx)
```

### Build do backend sem Java na máquina

```powershell
docker run --rm -v C:\Projetos\app-mobile\backend:/app -w /app maven:3.9-eclipse-temurin-21-alpine mvn clean install
```

| Parte | O que faz |
|-------|-----------|
| `docker run --rm` | Container temporário, destrói ao terminar |
| `-v C:\...\backend:/app` | Monta pasta local dentro do container |
| `-w /app` | Diretório de trabalho |
| `maven:3.9-eclipse-temurin-21-alpine` | Imagem com Maven 3.9 + Java 21 |
| `mvn clean install` | Comando executado |

O resultado (`target/`) aparece na sua máquina porque a pasta está montada com `-v`.

### Gerar APKs via Docker

```powershell
docker compose run --rm flutter-builder
# APKs em: mobile/apk-output/
#   - motoboy-app.apk
#   - store-app.apk
```

### Dockerfiles do projeto

| Arquivo | Tipo | Stage 1 (build) | Stage 2 (runtime) |
|---------|------|-----------------|-------------------|
| `backend/Dockerfile` | Multi-stage | `eclipse-temurin:21-jdk-alpine` + Maven | `eclipse-temurin:21-jre-alpine` |
| `admin/admin-angular/Dockerfile` | Multi-stage | `node:22-alpine` + `ng build` | `nginx:alpine` |
| `mobile/Dockerfile` | Multi-stage | `cirruslabs/flutter:3.29.3` + `flutter build apk` | `alpine:3.20` (só copia APKs) |

### Comandos Docker úteis

```powershell
docker compose up -d postgres redis rabbitmq       # Só infra
docker compose up -d --build backend               # Rebuild backend
docker compose logs -f backend                     # Logs em tempo real
docker compose down                                # Parar tudo
docker compose down -v                             # Parar + APAGAR volumes (dados do banco!)
docker ps                                          # Listar containers
docker compose restart backend                     # Reiniciar um serviço
docker exec -it delivery_postgres psql -U postgres -d delivery_db   # Abrir psql
```

---

## 10 — Configurações e Variáveis

### Backend — `application.properties`

| Propriedade | Valor padrão | Descrição |
|-------------|-------------|-----------|
| `server.port` | `8080` | Porta da API |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/delivery_db` | URL do banco |
| `spring.datasource.username` | `postgres` | User do banco |
| `spring.datasource.password` | `postgres` | Senha do banco |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Schema management (local: `update`) |
| `spring.flyway.enabled` | `true` | Migrations automáticas |
| `spring.data.redis.host` | `localhost` | Host Redis |
| `spring.data.redis.port` | `6379` | Porta Redis |
| `spring.rabbitmq.host` | `localhost` | Host RabbitMQ |
| `spring.rabbitmq.port` | `5672` | Porta RabbitMQ |
| `spring.rabbitmq.username` | `guest` | User RabbitMQ |
| `spring.rabbitmq.password` | `guest` | Senha RabbitMQ |
| `jwt.secret` | (trocar em produção!) | Chave para assinar JWT (min 256 bits) |
| `jwt.expiration` | `86400000` | Expiração JWT (24h em ms) |
| `pagarme.api.url` | `https://api.pagar.me/core/v5` | URL API Pagar.me |
| `pagarme.api.key` | (sua chave) | API key Pagar.me |
| `cors.allowed-origins` | `http://localhost:4200,http://localhost:3000` | Origens CORS |
| `firebase.credentials.path` | `src/main/resources/firebase-credentials.json` | Credenciais Firebase |
| `delivery.queue.entrega` | `fila.entrega` | Nome fila RabbitMQ |
| `delivery.queue.notificacao` | `fila.notificacao` | Nome fila RabbitMQ |
| `delivery.queue.pagamento` | `fila.pagamento` | Nome fila RabbitMQ |

### Backend dentro do Docker — Hosts mudam

Dentro do Docker, os hosts são os **nomes dos services** do `docker-compose.yml`:

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/delivery_db   # "postgres" = service name
SPRING_DATA_REDIS_HOST: redis                                         # "redis" = service name
SPRING_RABBITMQ_HOST: rabbitmq                                        # "rabbitmq" = service name
```

### Angular — `environments/`

**Dev** (`environment.ts`):
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  wsUrl: 'http://localhost:8080/ws'
};
```

**Produção** (`environment.prod.ts`):
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://sua-api.com/api',
  wsUrl: 'https://sua-api.com/ws'
};
```

### Flutter — URL da API nos services

```dart
// Emulador Android:
const _baseUrl = 'http://10.0.2.2:8080/api';    // 10.0.2.2 = localhost do host no emulador

// Device físico na mesma rede Wi-Fi:
const _baseUrl = 'http://192.168.1.X:8080/api';  // IP real da máquina (ipconfig → IPv4)

// Produção:
const _baseUrl = 'https://sua-api.com/api';
```

> ⚠️ No emulador Android, `localhost` aponta para o PRÓPRIO emulador.
> Use `10.0.2.2` para alcançar o host (sua máquina Windows).

---

## 11 — Endpoints da API

| Método | Endpoint | Quem usa | Descrição |
|--------|----------|----------|-----------|
| `POST` | `/api/auth/login` | Todos | Login → retorna JWT |
| `GET` | `/api/lojas` | Admin | Listar lojas |
| `POST` | `/api/lojas` | Admin | Criar loja |
| `GET` | `/api/motoboys` | Admin | Listar motoboys |
| `POST` | `/api/motoboys` | Admin | Criar motoboy |
| `PATCH` | `/api/motoboys/{id}/localizacao` | Motoboy | Atualizar GPS |
| `GET` | `/api/pedidos/loja/{id}` | Loja, Admin | Pedidos da loja |
| `POST` | `/api/pedidos` | Loja, Admin | Criar pedido |
| `PATCH` | `/api/pedidos/{id}/status` | Todos | Mudar status |
| `GET` | `/api/entregas/pendentes` | Admin | Entregas sem motoboy |
| `GET` | `/api/entregas/motoboy/{id}` | Motoboy | Minhas entregas |
| `POST` | `/api/entregas/pedido/{id}` | Loja, Admin | Criar entrega |
| `PATCH` | `/api/entregas/{id}/atribuir` | Admin | Atribuir motoboy à entrega |
| `PATCH` | `/api/entregas/{id}/coletar` | Motoboy | Confirmar coleta na loja |
| `PATCH` | `/api/entregas/{id}/finalizar` | Motoboy | Finalizar entrega |
| `POST` | `/api/pagamentos/processar` | Motoboy | Registrar pagamento (maquininha) |
| `GET` | `/api/pagamentos/liquidacoes/loja/{id}` | Admin, Loja | Histórico de liquidações |
| `POST` | `/api/pagamentos/liquidar-agora` | Admin | Liquidação manual (dev) |

---

## 12 — Fluxo de Telas

### Motoboy App (Flutter)

```
LoginScreen
    ↓ login OK (JWT salvo no Secure Storage)
HomeScreen (lista de entregas do dia)
    ↓ toca em entrega ATRIBUIDA ou COLETADA
EntregaAtivaScreen
    ├── Status ATRIBUIDA → botão "Confirmar coleta na loja"
    │       ↓ clica
    │   Status vira COLETADA → botão "Cobrar R$ XX"
    │       ↓ clica
    │   Diálogo de confirmação → "Ativar maquininha"
    │       ↓ SDK Smart POS processa pagamento
    │   processarPagamento() → finalizarEntrega()
    └── Volta para HomeScreen
```

### Store App (Flutter)

```
LoginScreen
    ↓ login OK
PedidosScreen (tabs: Ativos | Todos)
    ├── Lista de pedidos com status colorido
    ├── FAB "Novo Pedido" → cria pedido via API
    └── Pull-to-refresh para atualizar
```

### Admin Angular (Web)

```
/login → LoginComponent
    ↓ login OK (JWT salvo, interceptor adiciona em toda request)
LayoutComponent (sidebar com menu)
    ├── /dashboard    → Métricas + atribuição de motoboys a entregas pendentes
    ├── /pedidos      → Lista de todos os pedidos (filtro por status)
    ├── /motoboys     → CRUD de motoboys (cadastrar, editar, desativar)
    ├── /lojas        → CRUD de lojas (cadastrar, editar)
    └── /relatorios   → Liquidações diárias por loja (valores, taxas)
```

---

## 13 — Entidades do Banco

```
Usuario        → id, email, senha (BCrypt), nome, role (ADMIN/LOJA/MOTOBOY), ativo, criadoEm
Loja           → id, nome, cnpj, chavePix, saldo
Motoboy        → id, nome, cnh, smartPosSerial, status (DISPONIVEL/EM_ENTREGA/OFFLINE)
Pedido         → id, loja, itens, valorTotal, status (CRIADO/ACEITO/EM_ENTREGA/ENTREGUE/CANCELADO)
Entrega        → id, pedido, motoboy, status (PENDENTE/ATRIBUIDA/COLETADA/ENTREGUE), timestampColeta
Transacao      → id, entrega, valor, maquininha, status (PENDENTE/APROVADA/RECUSADA), liquidada
LiquidacaoDia  → id, loja, data, valorBruto, taxa, valorLiquido
```

O Flyway gerencia a criação e evolução do schema via scripts em:
```
backend/src/main/resources/db/migration/
  V1__create_tables.sql
  V2__add_columns.sql
  ...
```

---

## 14 — Modelo de Negócio e Jurídico

### Como funciona o dinheiro

Modelo **Marketplace** com split automático via Pagar.me — a plataforma **nunca** recebe
o dinheiro da loja. O split é feito pelo processador de pagamento:

```
Cliente paga R$ 100 na maquininha
         ↓
    Pagar.me processa
         ↓ split automático
   R$ 92,50 → Conta da Loja (direto, D+1/D+2)
   R$  5,00 → Sua conta (taxa de plataforma, 5%)
   R$  2,50 → Taxa Pagar.me (2,5% cartão)
```

### Por que esse modelo

Se o dinheiro cai na **sua conta** e você repassa, o Banco Central considera:
- **Instituição de Pagamento não autorizada** → multa pesada
- **Intermediador financeiro informal** → encerramento compulsório
- Lei 12.865/2013 + Resolução BCB 80/2021

Com o split via Pagar.me/Zoop, você é um **Marketplace** (legal e regulamentado).

### Plataformas de pagamento

| Plataforma | Vantagem |
|-----------|----------|
| **Pagar.me** (Stone) | Melhor docs, marketplace model nativo, split automático |
| **Zoop** | Foco delivery/marketplace, SDK para maquininha |
| **Celcoin** | Voltado fintech, split via API |

### Integração técnica (Pagar.me)

```java
// 1. Cadastrar loja como recebedor
POST /recipients  { name, document, bank_account: {...} }

// 2. Criar cobrança com split
POST /transactions {
  amount: 10000,   // R$ 100,00 em centavos
  split_rules: [
    { recipient_id: "loja_abc", percentage: 93 },
    { recipient_id: "plataforma_xyz", percentage: 7 }
  ]
}
```

### Requisitos legais

| Item | Detalhe |
|------|---------|
| **CNPJ** | CNAE `6209-1/00` (serviços de TI) + `5250-8/05` (operadores logísticos) |
| **Modelo** | Marketplace — intermediador logístico, NÃO financeiro |
| **Contrato lojas** | Deixar claro que é plataforma de intermediação |
| **Contrato motoboys** | PJ (MEI) para evitar vínculo CLT |
| **Advogado** | Especialista em direito digital/fintech (R$ 2-5k) |

---

## 15 — Troubleshooting

### Docker não sobe / erro de WSL

```powershell
# Docker Desktop está rodando?
docker info

# WSL está ativo?
wsl --list --verbose

# Virtualização ativa?
systeminfo | Select-String "Hyper-V"
# "Virtualization Enabled In Firmware: No" → ativar na BIOS
```

### Backend não conecta no banco

```powershell
# PostgreSQL rodando?
docker ps | Select-String "postgres"

# Testar conexão
docker exec -it delivery_postgres psql -U postgres -d delivery_db -c "SELECT 1"
```

- Dev local → `spring.datasource.url=jdbc:postgresql://localhost:5432/delivery_db`
- Dentro Docker → `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/delivery_db`

### Angular não conecta na API

1. Backend rodando em `:8080`?
2. `environment.ts` → `apiUrl: 'http://localhost:8080/api'`?
3. Backend CORS inclui `http://localhost:4200`?

### Flutter — `10.0.2.2` não funciona

- Só funciona no **emulador** Android, NÃO em device físico
- Device físico: usar IP real (`ipconfig` → IPv4 Address)
- Firewall bloqueando porta 8080? Desativar temporariamente para testar

### Flutter — erro ao compilar

```powershell
flutter clean
flutter pub get
flutter run
```

Se persistir, regenerar estrutura:
```powershell
flutter create --project-name motoboy_app --org com.delivery .
```

### Porta em uso

```powershell
# Quem está na porta 8080?
netstat -ano | Select-String "8080"

# Matar processo
taskkill /PID <numero> /F
```

### Limpar TUDO do Docker

```powershell
docker compose down -v          # Para tudo + apaga volumes
docker system prune -af         # Remove imagens/containers parados (⚠️ cuidado)
```

---

## 16 — Próximos Passos

### Feito ✅

- [x] Estrutura monorepo definida
- [x] Docker Compose (PostgreSQL + Redis + RabbitMQ)
- [x] Backend Spring Boot com todas as dependências
- [x] Flyway para migrations
- [x] Admin Angular com Material UI + todas as telas
- [x] Flutter motoboy_app e store_app com telas base
- [x] Dockerfiles multi-stage para todos os componentes
- [x] Documentação completa (README + INSTRUCTIONS)

### Pendente 🔜

- [ ] Configurar Firebase (google-services.json)
- [ ] Integrar SDK da maquininha Stone/Pagar.me no Flutter
- [ ] Implementar tela de criação de pedidos no store_app
- [ ] Webhook do Pagar.me para confirmação de pagamento
- [ ] Testes automatizados (JUnit + Mockito + SpringBootTest)
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Deploy em produção (VPS / Cloud)

---

## 📚 Links Úteis

| Recurso | URL |
|---------|-----|
| Spring Boot Docs | https://docs.spring.io/spring-boot/docs/current/reference/html/ |
| Angular Docs | https://angular.dev |
| Flutter Docs | https://docs.flutter.dev |
| Pagar.me API Docs | https://docs.pagar.me |
| Docker Compose Docs | https://docs.docker.com/compose/ |
| Flyway Docs | https://documentation.red-gate.com/fd |
| RabbitMQ Tutorials | https://www.rabbitmq.com/tutorials |
| Firebase Flutter Setup | https://firebase.google.com/docs/flutter/setup |

