# 🛵 Delivery App — Plataforma com Smart POS

Stack: **Java 21 + Spring Boot 3** | **Angular 17** | **Flutter 3** | **PostgreSQL** | **Redis** | **RabbitMQ** | **Firebase** | **Pagar.me**

---

## 📁 Estrutura do Projeto

```
delivery-app/
├── backend/              ← Spring Boot 3 + Java 21 (API REST)
├── admin/
│   └── admin-angular/    ← Angular 17 (painel web admin)
├── mobile/
│   ├── motoboy_app/      ← Flutter (Android Smart POS)
│   └── store_app/        ← Flutter (Android/iOS loja)
└── docker-compose.yml    ← PostgreSQL + Redis + RabbitMQ
```

---

## 🚀 Pré-requisitos e Instalação

### 1. Infraestrutura (Docker)

```bash
# Subir PostgreSQL + Redis + RabbitMQ de uma vez:
docker-compose up -d

# Verificar se subiu:
docker ps
# Deve mostrar: delivery_postgres, delivery_redis, delivery_rabbitmq
```

| Serviço    | URL / Porta               |
|------------|---------------------------|
| PostgreSQL | `localhost:5432`          |
| Redis      | `localhost:6379`          |
| RabbitMQ   | `localhost:5672`          |
| RabbitMQ UI| http://localhost:15672 (guest/guest) |

---

### 2. Java + Spring Boot (Backend)

**Pré-requisitos:**
```bash
java -version      # precisa ser 21+
mvn -version       # Maven 3.9+
```

**Rodar:**
```bash
cd backend
mvn spring-boot:run
# API disponível em http://localhost:8080
```

O Hibernate cria as tabelas automaticamente (`ddl-auto=update`).

**Inserir primeiro usuário admin:**
```sql
-- senha: admin123 (hash BCrypt)
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

---

### 3. Angular 17 (Admin Web)

**Pré-requisitos:**
```bash
node -v    # precisa ser 18+
npm -v     # 9+
```

**Rodar:**
```bash
cd admin/admin-angular
npm install
npm start
# Painel disponível em http://localhost:4200
```

Login com: `admin@delivery.com` / `admin123`

---

### 4. Flutter — GUIA COMPLETO PARA INICIANTES ⚠️

> Flutter é o framework mobile do Google que compila para Android/iOS nativos.
> Usa a linguagem **Dart** (muito parecida com Java/TypeScript).

---

#### 🔰 O que é Flutter? (para quem vem de Java/Angular)

| Conceito Java/Angular | Equivalente Flutter |
|---|---|
| `pom.xml` / `package.json` | `pubspec.yaml` (dependências) |
| `mvn install` / `npm install` | `flutter pub get` |
| `mvn spring-boot:run` / `ng serve` | `flutter run` |
| `@Component` (Angular) | `StatelessWidget` / `StatefulWidget` |
| `Observable` / `BehaviorSubject` | `ChangeNotifier` + `Provider` |
| `HttpClient` (Angular) | pacote `http` ou `dio` |
| `@Injectable()` | `ChangeNotifierProvider` |
| Layouts HTML/CSS | Widgets (`Column`, `Row`, `Container`, `Card`) |

**Dart vs Java — Quick cheat sheet:**
```dart
// Java:   public String getNome() { return nome; }
// Dart:   String get nome => _nome;

// Java record: record Pedido(int id, String cliente) {}
// Dart:
class Pedido {
  final int id;
  final String cliente;
  const Pedido({required this.id, required this.cliente});
}

// async/await — igual ao Java CompletableFuture, mas mais limpo:
Future<void> carregar() async {
  final res = await http.get(Uri.parse(url));
}

// null safety — parecido com Optional<T>:
String? nomeOpcional;  // pode ser null
String nomeObrigatorio; // nunca null
```

---

#### Passo 1 — Instalar Flutter SDK no Windows

1. **Baixe o Flutter SDK:**
   - https://docs.flutter.dev/get-started/install/windows
   - Baixe o `.zip` e extraia em `C:\flutter`

2. **Adicionar ao PATH:**
   - Pesquise "variáveis de ambiente" no Windows
   - Em "Path" do usuário, adicione: `C:\flutter\bin`
   - Abra um **novo terminal** para aplicar

3. **Verificar:**
   ```powershell
   flutter --version
   # Deve mostrar: Flutter 3.x.x
   ```

---

#### Passo 2 — Instalar Android SDK

O Android SDK é **necessário** para compilar para Android (maquininha smart).

1. **Baixe o Android Studio:** https://developer.android.com/studio
2. Instale e abra → **More Actions** → **SDK Manager**
3. Na aba **SDK Platforms**: marque **Android 14.0 (API 34)**
4. Na aba **SDK Tools**: marque **Android SDK Command-line Tools**
5. Clique **Apply** → Download

> ⚠️ Você **NÃO precisa usar o Android Studio para codificar**.
> É só para o SDK. Pode codificar no **IntelliJ** com plugin Flutter.

---

#### Passo 3 — Aceitar licenças e verificar

```powershell
flutter doctor --android-licenses
# Responda "y" para tudo

flutter doctor
# Deve mostrar:
#   [✓] Flutter
#   [✓] Android toolchain
#   [✓] Android Studio (ou VS Code / IntelliJ)
```

Se aparecer ✗ em algum, o próprio `flutter doctor` diz como resolver.

---

#### Passo 4 — Plugin Flutter no IntelliJ

1. IntelliJ IDEA → **File → Settings → Plugins**
2. Buscar: **Flutter** → Instalar (instala Dart automaticamente)
3. Reiniciar o IntelliJ
4. Ao abrir `motoboy_app/`, o IntelliJ detecta o `pubspec.yaml`

---

#### Passo 5 — Gerar estrutura Android dos projetos Flutter

Os projetos Flutter neste repo contêm apenas o código Dart (`lib/` e `pubspec.yaml`).
A pasta `android/` precisa ser gerada pelo Flutter:

```powershell
# Motoboy App
cd mobile/motoboy_app
flutter create --project-name motoboy_app --org com.delivery .
flutter pub get

# Store App
cd ../store_app
flutter create --project-name store_app --org com.delivery .
flutter pub get
```

> O comando `flutter create .` **NÃO sobrescreve** arquivos existentes (`lib/main.dart`, `pubspec.yaml`).
> Ele apenas gera o que está faltando (`android/`, `ios/`, `test/`, etc).

---

#### Passo 6 — Criar emulador ou conectar device

**Opção A — Emulador Android (recomendado para desenvolvimento):**
1. Android Studio → **More Actions** → **Virtual Device Manager**
2. **Create Device** → escolha **Pixel 7** → **Next**
3. Escolha **API 34** → **Next** → **Finish**
4. Clique ▶ para iniciar o emulador

**Opção B — Dispositivo físico:**
1. Ativar **Opções de desenvolvedor** no Android (tocar 7x em "Número da compilação")
2. Ativar **Depuração USB**
3. Conectar via USB → aceitar "Permitir depuração"

```powershell
# Listar devices disponíveis:
flutter devices
```

---

#### Passo 7 — Rodar os apps Flutter

```powershell
# Motoboy App
cd mobile/motoboy_app
flutter pub get     # baixa dependências (= npm install)
flutter run         # compila e roda no emulador/device

# Store App (em outro terminal)
cd mobile/store_app
flutter pub get
flutter run
```

**Comandos úteis:**
```powershell
flutter pub get          # baixar dependências
flutter run              # rodar app
flutter run -d chrome    # rodar no navegador (debug rápido)
flutter build apk        # gerar APK para instalar
flutter clean            # limpar cache (se der erro estranho)
flutter doctor           # diagnóstico geral
```

**Hot Reload:**
- Quando o app está rodando, pressione `r` no terminal = Hot Reload (atualiza sem perder estado)
- Pressione `R` = Hot Restart (reinicia o app)
- Isso é **muito** mais rápido que recompilar Java

---

#### Passo 8 — Configurar Firebase (opcional — para notificações push)

1. Acesse https://console.firebase.google.com
2. Crie um projeto chamado `delivery-app`
3. Adicione app Android: package `com.delivery.motoboy_app`
4. Baixe `google-services.json` → coloque em:
   - `mobile/motoboy_app/android/app/google-services.json`
   - `mobile/store_app/android/app/google-services.json`
5. Para o backend: Firebase Console → Configurações → Contas de serviço → Gerar chave
   - Salve como `backend/src/main/resources/firebase-credentials.json`

> **Sem Firebase configurado**, os apps funcionam normalmente — só não enviam/recebem push.

---

## 🔧 Variáveis de configuração

### Backend — `application.properties`
| Propriedade | O que colocar |
|---|---|
| `spring.datasource.password` | senha do seu PostgreSQL |
| `jwt.secret` | string aleatória longa (min 32 chars) |
| `pagarme.api.key` | chave da conta Pagar.me |
| `cors.allowed-origins` | URL do Angular em produção |

### Flutter — `lib/services/auth_service.dart`
```dart
// Emulador Android (padrão):
const _baseUrl = 'http://10.0.2.2:8080/api';  // 10.0.2.2 = localhost do emulador

// Dispositivo físico na mesma rede:
const _baseUrl = 'http://192.168.1.X:8080/api';  // IP da máquina

// Produção:
const _baseUrl = 'https://sua-api.com/api';
```

---

## 🗺️ Fluxo de telas — Motoboy App

```
LoginScreen
    ↓ login OK
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

## 🗺️ Fluxo de telas — Store App

```
LoginScreen
    ↓ login OK
PedidosScreen (tabs: Ativos | Todos)
    ├── Lista de pedidos com status colorido
    ├── FAB "Novo Pedido" → cria pedido via API
    └── Pull-to-refresh para atualizar
```

## 🗺️ Telas — Admin Angular

```
LoginComponent
    ↓ login OK
LayoutComponent (sidebar com menu)
    ├── /dashboard    → Dashboard com métricas + atribuição de motoboys
    ├── /pedidos      → Lista de todos os pedidos (filtro por status)
    ├── /motoboys     → CRUD de motoboys
    ├── /lojas        → CRUD de lojas
    └── /relatorios   → Liquidações diárias por loja
```

---

## 🔌 Endpoints da API (resumo)

| Método | Endpoint | Quem usa | Descrição |
|--------|----------|----------|-----------|
| POST | `/api/auth/login` | Todos | Login (retorna JWT) |
| GET | `/api/lojas` | Admin | Listar lojas |
| POST | `/api/lojas` | Admin | Criar loja |
| GET | `/api/motoboys` | Admin | Listar motoboys |
| POST | `/api/motoboys` | Admin | Criar motoboy |
| PATCH | `/api/motoboys/{id}/localizacao` | Motoboy | Atualizar GPS |
| GET | `/api/pedidos/loja/{id}` | Loja, Admin | Pedidos da loja |
| POST | `/api/pedidos` | Loja, Admin | Criar pedido |
| PATCH | `/api/pedidos/{id}/status` | Todos | Mudar status |
| GET | `/api/entregas/pendentes` | Admin | Entregas sem motoboy |
| GET | `/api/entregas/motoboy/{id}` | Motoboy | Minhas entregas |
| POST | `/api/entregas/pedido/{id}` | Loja, Admin | Criar entrega |
| PATCH | `/api/entregas/{id}/atribuir` | Admin | Atribuir motoboy |
| PATCH | `/api/entregas/{id}/coletar` | Motoboy | Confirmar coleta |
| PATCH | `/api/entregas/{id}/finalizar` | Motoboy | Finalizar entrega |
| POST | `/api/pagamentos/processar` | Motoboy | Registrar pagamento |
| GET | `/api/pagamentos/liquidacoes/loja/{id}` | Admin, Loja | Histórico liquidações |
| POST | `/api/pagamentos/liquidar-agora` | Admin | Liquidação manual (dev) |

---

## 📋 Próximos passos (ordem sugerida)

1. ✅ Subir infraestrutura com `docker-compose up -d`
2. ✅ Rodar o backend: `cd backend && mvn spring-boot:run`
3. ✅ Inserir usuário admin no banco (SQL acima)
4. ✅ Rodar o Angular: `cd admin/admin-angular && npm install && npm start`
5. ✅ Instalar Flutter SDK + rodar `flutter doctor`
6. ✅ Gerar estrutura Android: `flutter create .` dentro de cada app
7. ✅ Rodar `motoboy_app` e `store_app` no emulador
8. 🔜 Configurar Firebase (google-services.json)
9. 🔜 Integrar SDK da maquininha Stone/Pagar.me
10. 🔜 Implementar tela de criação de pedidos no store_app
11. 🔜 Webhook do Pagar.me para confirmação de pagamento
