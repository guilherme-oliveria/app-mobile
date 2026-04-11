# 🛵 Agent Motoboy App — Flutter (Smart POS)

> Agente especialista no app do motoboy do Delivery App.

---

## Identidade

Você é o agente responsável pelo **motoboy_app**, o aplicativo Flutter usado pelos motoboys da plataforma.
Este app roda em **celulares Android comuns** e em **maquininhas Smart POS** (Stone).
É o app que aceita entregas, confirma coleta, cobra o cliente e finaliza a entrega.

---

## Tech Stack

| Tecnologia | Versão | Função |
|-----------|--------|--------|
| Flutter | 3.29 | Framework mobile |
| Dart | >=3.3 | Linguagem |
| Provider | 6.1 | State management |
| http | 1.2 | HTTP client (chamadas à API) |
| dio | 5.4 | HTTP client (alternativo) |
| flutter_secure_storage | 9.0 | Armazenamento seguro do JWT |
| geolocator | 11.0 | GPS — localização do motoboy |
| stomp_dart_client | 0.4 | WebSocket STOMP (tempo real) |
| intl | 0.19 | Formatação de datas/moedas |
| firebase_core | 2.27 | ⚠️ **Comentado** — descomentar quando configurar FCM |
| firebase_messaging | 14.7 | ⚠️ **Comentado** — push notifications |

---

## Estrutura de Pastas

```
motoboy_app/lib/
├── main.dart                          ← Entry point — Consumer decide: Login → TrocarSenha → Home
├── models/
│   └── models.dart                    ← Usuario, Entrega, Pedido, ItemPedido, StatusEntrega (enum)
├── screens/
│   ├── login_screen.dart              ← Tela de login (email + senha)
│   ├── trocar_senha_screen.dart       ← Troca de senha (obrigatória no 1º login + menu)
│   ├── home_screen.dart               ← Tabs: Disponíveis + Minhas Entregas + PopupMenu
│   └── entrega_ativa_screen.dart      ← Detalhes da entrega + botões: Coletar → Cobrar → Finalizar
├── services/
│   ├── auth_service.dart              ← Login, logout, trocarSenha, validação de role
│   └── entrega_service.dart           ← CRUD entregas, aceitar, coletar, finalizar, pagamento
└── widgets/                           ← Componentes reutilizáveis (a implementar)
```

---

## Fluxo de Navegação

```
main.dart (Consumer<AuthService>)
    │
    ├── NÃO logado → LoginScreen
    │       └── login() → valida role (MOTOBOY ou ADMIN apenas)
    │
    ├── Logado + deveAlterarSenha → TrocarSenhaScreen(obrigatoria: true)
    │       └── trocarSenha() → notifyListeners() → Consumer reconstrói → HomeScreen
    │
    └── Logado + senha ok → HomeScreen
            ├── Tab "Disponíveis" → lista entregas DISPONIVEL → aceitar
            ├── Tab "Minhas Entregas" → lista entregas do motoboy
            ├── Toca numa entrega ativa → EntregaAtivaScreen
            │       ├── Confirmar coleta → status COLETADA
            │       ├── Cobrar → ⚠️ TODO: SDK Stone → processarPagamento
            │       └── Finalizar → status FINALIZADA
            └── PopupMenu → Trocar Senha / Sair
```

---

## Convenções Obrigatórias

### State Management
- **Provider** (`ChangeNotifierProvider` + `Consumer` / `context.read` / `context.watch`)
- `AuthService` e `EntregaService` são `ChangeNotifier`
- `notifyListeners()` após qualquer mudança de estado

### URL da API (detecção automática)
```dart
import 'package:flutter/foundation.dart' show kIsWeb;

const _baseUrl = kIsWeb
    ? 'http://localhost:8080/api'      // browser (Flutter Web)
    : 'http://10.0.2.2:8080/api';     // emulador Android
// Celular físico na rede: 'http://192.168.X.X:8080/api'
```

### Validação de Perfil no Login
- Roles permitidos: `['MOTOBOY', 'ADMIN']`
- Se LOJA tentar logar → "Este é o app do Motoboy. Use o app da Loja para acessar."
- Se SUPORTE tentar logar → "Acesso de suporte disponível apenas no painel web."

### Armazenamento
- JWT salvo com `FlutterSecureStorage` (key: `token`, `usuario`)
- Ao fazer logout, `deleteAll()`
- Ao abrir o app, tenta restaurar do storage (`_carregarDoStorage`)

### Troca de Senha
- Se `deveAlterarSenha == true` → `TrocarSenhaScreen(obrigatoria: true)` como home
- Se obrigatória, **NÃO faz `Navigator.pop()`** — o Consumer reconstrói automaticamente
- Se aberta pelo menu (obrigatória = false), faz `Navigator.pop()` normal

### Firebase (desabilitado para dev)
- `firebase_core` e `firebase_messaging` comentados no `pubspec.yaml`
- Import e `Firebase.initializeApp()` comentados no `main.dart`
- Para ativar: descomentar + adicionar `google-services.json` em `android/app/`

---

## Integração Smart POS (pendente)

O motoboy_app é o **mesmo APK** que roda no celular e na Smart POS (Stone).
A maquininha Smart POS roda Android — o APK Flutter funciona direto nela.

```dart
// entrega_ativa_screen.dart — linha 93
// TODO: integrar SDK da maquininha aqui (Stone SmartPOS SDK)
await service.processarPagamento(
  token: auth.token,
  entregaId: _entrega.id,
  valor: _entrega.pedido.valorTotal,
  smartPosSerial: 'SERIAL_DA_MAQUININHA', // ler do SharedPreferences
  formaPagamento: 'credit_card',
);
```

Quando integrar o SDK Stone:
1. Adicionar dependência `stone_pos` no `pubspec.yaml`
2. Chamar SDK antes de `processarPagamento()`
3. Só registrar no backend se o SDK retornar sucesso

---

## Como rodar

```bash
# Dev local
cd mobile/motoboy_app
flutter pub get
flutter run                    # emulador Android
flutter run -d chrome          # browser (sem GPS/storage)

# Gerar APK
flutter build apk --release

# Via Docker
docker compose --profile build run --rm flutter-builder
```

