# 📋 Delivery App — Documentação do Modelo de Negócio

> Documentação completa do projeto Delivery App (Smart POS).
> Cada arquivo cobre um aspecto do modelo de negócio, arquitetura e operação.

---

## Índice

| # | Arquivo | Conteúdo |
|---|---------|----------|
| 01 | [Visão Geral](01-visao-geral.md) | O que é o projeto, fluxo geral, apps e modelo financeiro resumido |
| 02 | [Perfis e Login](02-perfis-e-login.md) | Roles (ADMIN, LOJA, MOTOBOY), autenticação JWT, criação de credenciais |
| 03 | [Diagrama de Cadastros](03-diagrama-cadastros.md) | Quem cadastra quem, fluxo de onboarding de lojas e motoboys |
| 04 | [Ciclo de Vida do Pedido](04-ciclo-vida-pedido.md) | Passo a passo completo: do pedido criado até entrega + pagamento |
| 05 | [Diagrama de Entidades](05-diagrama-entidades.md) | Todas as tabelas do banco, colunas, tipos e relacionamentos |
| 06 | [Integrações Externas](06-diagrama-integracoes.md) | Pagar.me, Firebase, RabbitMQ, Redis, WebSocket — o que cada um faz |
| 07 | [Painel Admin](07-painel-admin.md) | Todas as telas do Angular, funcionalidades e endpoints consumidos |
| 08 | [Permissões (RBAC)](08-diagrama-permissoes.md) | Matriz completa: quem pode acessar cada endpoint |
| 09 | [Máquinas de Estado](09-maquinas-de-estado.md) | Status de Pedido, Entrega, Motoboy, Transação e Liquidação |
| 10 | [Fluxo Financeiro](10-fluxo-financeiro.md) | Da cobrança na maquininha até a liquidação na conta da loja |
| 11 | [Resumo Financeiro](11-resumo-financeiro.md) | Receita, custos, simulações e comparação com concorrentes |
| 12 | [Pendências e Roadmap](12-pendencias-roadmap.md) | O que está pronto, o que falta, checklist pré-produção |

---

## Como ler

- **Primeira vez no projeto?** Leia na ordem: 01 → 04 → 05 → 10
- **Quer entender o código?** Leia: 05 → 06 → 07 → 08
- **Quer saber o que falta?** Leia: 12
- **Quer apresentar para investidor/sócio?** Leia: 01 → 10 → 11

---

## Referências

- [INSTRUCTIONS.md](../.github/INSTRUCTIONS.md) — Documentação técnica completa (setup, Docker, comandos)
- [README.md](../README.md) — Guia rápido de setup

