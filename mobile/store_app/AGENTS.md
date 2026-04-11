# 🏪 Agent Store App — Flutter (App da Loja)

> Agente especialista no app da loja do Delivery App.

---

## Identidade

Você é o agente responsável pelo **store_app**, o aplicativo Flutter usado pelas lojas da plataforma.
É o app onde a loja acompanha seus pedidos, cria novos pedidos e monitora o status das entregas.

---

## Tech Stack

| Tecnologia | Versão | Função |
|-----------|--------|--------|
| Flutter | 3.29 | Framework mobile |
| Dart | >=3.3 | Linguagem |
| Provider | 6.1 | State management |
| http | 1.2 | HTTP client |
| flutter_secure_storage | 9.0 | Armazenamento seguro do JWT |
| intl | 0.19 | Formatação de datas/moedas |
| shared_preferences | 2.2 | Preferências locais |
| firebase_core | 2.27 | ⚠️ **Comentado** — descomentar quando configurar FCM |
| firebase_messaging | 14.7 | ⚠️ **Comentado** — push notifications |

---

## Estrutura de Pastas

```
store_app/lib/
├── main.dart                          ← Entry point — Consumer decide: Login → TrocarSenha → Pedidos
├── screens/
│   ├── login_screen.dart              ← Tela de login (email + senha) — cor azul (#2563EB)
│   ├── trocar_senha_screen.dart       ← Troca de senha (obrigatória no 1º login + menu)
│   └── pedidos_screen.dart            ← Tabs: Ativos + Todos + botão Novo Pedido
├── services/
│   ├── auth_service.dart              ← Login, logout, trocarSenha, validação de role + model Usuario
│   └── pedido_service.dart            ← CRUD pedidos, carregar por loja, criar, atualizar status
└── widgets/                           ← Componentes reutilizáveis (a implementar)
```

---

## Fluxo de Navegação

```
main.dart (Consumer<AuthService>)
    │
    ├── NÃO logado → LoginScreen
    │       └── login() → valida role (LOJA ou ADMIN apenas)
    │
    ├── Logado + deveAlterarSenha → TrocarSenhaScreen(obrigatoria: true)
    │       └── trocarSenha() → notifyListeners() → Consumer reconstrói → PedidosScreen
    │
    └── Logado + senha ok → PedidosScreen
            ├── Tab "Ativos" → pedidos em andamento
            ├── Tab "Todos" → histórico completo
            ├── FAB "Novo Pedido" → ⚠️ TODO: tela de criação
            └── PopupMenu → Trocar Senha / Sair
```

---

## Convenções Obrigatórias

### State Management
- **Provider** (`ChangeNotifierProvider` + `Consumer` / `context.read` / `context.watch`)
- `AuthService` e `PedidoService` são `ChangeNotifier`
- Model `Usuario` é definido dentro de `auth_service.dart` (não tem arquivo `models.dart` separado)

### URL da API (detecção automática)
```dart
import 'package:flutter/foundation.dart' show kIsWeb;

const _baseUrl = kIsWeb
    ? 'http://localhost:8080/api'      // browser (Flutter Web)
    : 'http://10.0.2.2:8080/api';     // emulador Android
```

### Validação de Perfil no Login
- Roles permitidos: `['LOJA', 'ADMIN']`
- Se MOTOBOY tentar logar → "Este é o app da Loja. Use o app do Motoboy para acessar."
- Se SUPORTE tentar logar → "Acesso de suporte disponível apenas no painel web."

### Armazenamento
- JWT salvo com `FlutterSecureStorage` (key: `usuario` — JSON completo)
- Ao abrir o app, tenta restaurar do storage

### Troca de Senha
- Se `deveAlterarSenha == true` → `TrocarSenhaScreen(obrigatoria: true)` como home
- Se obrigatória, **NÃO faz `Navigator.pop()`** — o Consumer reconstrói automaticamente
- Se aberta pelo menu (obrigatória = false), faz `Navigator.pop()` normal

### Identidade Visual
- Cor principal: **Azul** `#2563EB` (diferencia do motoboy_app que é laranja)
- Ícone do app: `Icons.storefront`
- Material3 habilitado

### Firebase (desabilitado para dev)
- Comentado no `pubspec.yaml` e no `main.dart`
- Para ativar: descomentar + adicionar `google-services.json`

---

## Diferenças com o motoboy_app

| Aspecto | motoboy_app | store_app |
|---------|------------|-----------|
| Cor | 🟠 Laranja (#FF6B00) | 🔵 Azul (#2563EB) |
| Role | MOTOBOY + ADMIN | LOJA + ADMIN |
| Foco | Entregas, coleta, pagamento | Pedidos, acompanhamento |
| GPS | ✅ Geolocator | ❌ Não precisa |
| WebSocket | ✅ STOMP | ❌ Não implementado (pode adicionar) |
| Pagamento | ✅ Smart POS | ❌ Não se aplica |
| Model | `models.dart` separado | `Usuario` dentro do `auth_service.dart` |

---

## Pendências do store_app

| Item | Prioridade | Detalhes |
|------|-----------|---------|
| Tela de criação de pedido | 🟡 Média | Hoje o FAB mostra SnackBar "a implementar" |
| Tela de detalhes do pedido | 🟡 Média | Hoje só lista — não tem drill-down |
| WebSocket para tempo real | 🟢 Baixa | Receber status do pedido em tempo real |
| Push notifications | 🔴 Alta (prod) | Avisar quando motoboy aceitar/coletar/entregar |

---

## Como rodar

```bash
# Dev local
cd mobile/store_app
flutter pub get
flutter run                    # emulador Android
flutter run -d chrome          # browser

# Gerar APK
flutter build apk --release

# Via Docker
docker compose --profile build run --rm flutter-builder
```

