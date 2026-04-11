# 10 — Fluxo Financeiro Completo

> De quando o cliente paga na maquininha até o dinheiro cair na conta da loja.

---

## Visão Geral

```
       VENDA NA MAQUININHA                    LIQUIDAÇÃO DIÁRIA (23:30h)
       ─────────────────                      ──────────────────────────

   Cliente insere cartão                  PagamentoService.liquidarDiario()
           │                                        │
           ▼                                        ▼
   SDK Smart POS (Stone)                  Busca transacoes WHERE
           │                              liquidada=false AND status=APROVADA
           ▼                                        │
   Pagar.me cria order                              ▼
   com split_rules                        Agrupa por loja
           │                                        │
    ┌──────┴──────┐                                 ▼
    │   SPLIT     │                         Para cada loja:
    │ automático  │                         ┌───────────────────────┐
    │             │                         │ totalBruto = Σ valor  │
    │ 92.5% Loja  │                         │ totalTaxas = Σ taxas  │
    │  5.0% Você  │                         │ líquido = bruto-taxas │
    │  2.5% Pgme  │                         │                       │
    └──────┬──────┘                         │ Pagar.me: transfere   │
           │                                │ líquido → conta loja  │
           ▼                                │                       │
   Transacao salva:                         │ LiquidacaoDia salva   │
    status=APROVADA                         │ Transações marcadas   │
    liquidada=false                         │ liquidada=true        │
                                            └───────────────────────┘
                                                      │
                                                      ▼
                                            Admin vê em /relatorios
                                            (bruto, taxas, líquido por loja/dia)
```

---

## Etapa 1: Pagamento na Maquininha

O motoboy chega no cliente e toca em "Cobrar" no app:

```
entrega_ativa_screen.dart
    │
    ├── Exibe diálogo: "Cobrar R$ 89,90?"
    ├── SDK Smart POS (Stone) processa o cartão
    └── Chama backend: POST /api/pagamentos/processar
        {
          "entregaId": 42,
          "valor": 89.90,
          "smartPosSerial": "STONE-ABC123",
          "formaPagamento": "credit_card"
        }
```

### O que o backend faz (PagamentoService.java)

```
processarPagamento(request):
    │
    ├── Busca a entrega (EntregaService)
    ├── Calcula split:
    │     valorBruto    = R$ 89,90
    │     taxaGateway   = R$  2,25  (2,5%)
    │     taxaPlataforma = R$  4,50  (5,0%)
    │     valorLiquido  = R$ 83,15
    │
    ├── Chama Pagar.me API:
    │     POST /orders com split_rules
    │     recipientId da loja + recipientId da plataforma
    │     Retorna: pagarmeTransactionId
    │
    └── Salva Transacao:
          status = APROVADA
          liquidada = false
```

---

## Etapa 2: Liquidação Diária

Executada automaticamente às **23:30** via `@Scheduled`:

```java
@Scheduled(cron = "0 30 23 * * *")
public void liquidarDiario() {
    // 1. Busca transações aprovadas e não liquidadas
    var pendentes = transacaoRepository
        .findByLiquidadaFalseAndStatus(APROVADA);

    // 2. Agrupa por loja
    var porLoja = pendentes.stream()
        .collect(groupingBy(t -> t.getEntrega().getPedido().getLoja()));

    // 3. Para cada loja
    porLoja.forEach((loja, transacoes) -> {
        var totalBruto = soma(transacoes, Transacao::getValorBruto);
        var totalTaxas = soma(transacoes, t -> t.getTaxaGateway() + t.getTaxaPlataforma());
        var valorLiquido = totalBruto - totalTaxas;

        // 4. Transfere via Pagar.me
        var transferId = pagarmeService.criarTransferencia(
            loja.getPagarmeRecipientId(), valorLiquido);

        // 5. Registra liquidação
        LiquidacaoDia.builder()
            .loja(loja)
            .dataReferencia(LocalDate.now())
            .totalEntregas(transacoes.size())
            .valorBruto(totalBruto)
            .totalTaxas(totalTaxas)
            .valorLiquido(valorLiquido)
            .pagarmeTransferId(transferId)
            .status(PROCESSADA)
            .build();

        // 6. Marca transações como liquidadas
        transacoes.forEach(t -> t.setLiquidada(true));
    });
}
```

---

## Tabela de Split — Para onde o dinheiro vai

| Para quem | Percentual | Em R$ 100,00 | Como recebe |
|-----------|-----------|-------------|-------------|
| **Loja** | 92,5% | R$ 92,50 | Direto do Pagar.me → conta bancária (D+1 ou D+2) |
| **Você** (plataforma) | 5,0% | R$ 5,00 | Fica na sua conta Pagar.me |
| **Pagar.me** (gateway) | 2,5% | R$ 2,50 | Retido automaticamente pelo gateway |

---

## Exemplo prático: dia com 10 entregas de uma loja

```
Loja: Pizzaria do João
Data: 2026-04-11
Entregas: 10

┌─────────┬───────────┬────────────┬──────────┬──────────────┐
│ Entrega │ Bruto     │ Taxa (5%)  │ Pgme(2.5)│ Líquido Loja │
├─────────┼───────────┼────────────┼──────────┼──────────────┤
│ #101    │ R$ 45,00  │ R$ 2,25    │ R$ 1,13  │ R$ 41,62     │
│ #102    │ R$ 89,90  │ R$ 4,50    │ R$ 2,25  │ R$ 83,15     │
│ #103    │ R$ 32,50  │ R$ 1,63    │ R$ 0,81  │ R$ 30,06     │
│ ...     │ ...       │ ...        │ ...      │ ...          │
├─────────┼───────────┼────────────┼──────────┼──────────────┤
│ TOTAL   │ R$ 850,00 │ R$ 42,50   │ R$ 21,25 │ R$ 786,25    │
└─────────┴───────────┴────────────┴──────────┴──────────────┘

Liquidação às 23:30:
  → Pagar.me transfere R$ 786,25 para conta PIX da Pizzaria
  → LiquidacaoDia registrada com status PROCESSADA
  → Admin vê no /relatorios
```

---

## Onde ver no admin

Tela **/relatorios** (`RelatoriosComponent`):
1. Seleciona "Pizzaria do João" no dropdown
2. Tabela mostra todas as liquidações diárias
3. Footer mostra totais acumulados
4. Pode liquidar manualmente com o botão "Liquidar agora" (uso dev/teste)

---

## Modelo jurídico (por que split)

```
❌ ERRADO (irregular):
   Cliente → Pagar.me → SUA conta → repasse manual → Conta da loja
   = Você é "Instituição de Pagamento" não autorizada pelo BCB

✅ CORRETO (split marketplace):
   Cliente → Pagar.me → Split automático → 92,5% direto para a loja
   = Você é um Marketplace intermediador LOGÍSTICO (não financeiro)
```

**Base legal:** Lei 12.865/2013 + Resolução BCB 80/2021
**Modelo:** Marketplace com split — mesma estrutura do iFood, Rappi, 99Food

