# 11 — Resumo Financeiro e Modelo de Receita

> Quanto você ganha, quanto a loja recebe, e os custos operacionais.

---

## Sua receita por transação

| Componente | % | Descrição |
|-----------|---|-----------|
| Taxa da plataforma | **5,0%** | Sua receita — fica na sua conta Pagar.me |
| Taxa do gateway (Pagar.me) | 2,5% | Retido pelo Pagar.me (custo operacional) |
| Valor líquido da loja | 92,5% | Vai direto para a conta da loja |

> As taxas são configuráveis em `PagamentoService.java`:
> ```java
> private static final BigDecimal TAXA_PLATAFORMA = new BigDecimal("0.05");
> private static final BigDecimal TAXA_GATEWAY = new BigDecimal("0.025");
> ```

---

## Simulação de receita mensal

### Cenário: 50 entregas/dia, ticket médio R$ 60

| Métrica | Valor |
|---------|-------|
| Entregas/dia | 50 |
| Ticket médio | R$ 60,00 |
| GMV diário (bruto) | R$ 3.000,00 |
| **Sua receita/dia (5%)** | **R$ 150,00** |
| **Sua receita/mês (30d)** | **R$ 4.500,00** |
| Custo Pagar.me/mês (2,5%) | R$ 2.250,00 |
| Loja recebe/mês (92,5%) | R$ 83.250,00 |

### Cenário: 200 entregas/dia (escala), ticket médio R$ 60

| Métrica | Valor |
|---------|-------|
| Entregas/dia | 200 |
| GMV diário | R$ 12.000,00 |
| **Sua receita/dia** | **R$ 600,00** |
| **Sua receita/mês** | **R$ 18.000,00** |

---

## Custos operacionais estimados

| Item | Custo mensal estimado |
|------|----------------------|
| Servidor VPS (backend + admin) | R$ 100-300 |
| Pagar.me (2,5% do GMV) | Variável |
| Firebase (push notifications) | Gratuito até 10k/dia |
| Domínio + SSL | R$ 30-50/ano |
| Maquininhas Smart POS (aluguel) | R$ 50-100/mês cada |
| **Total fixo mensal** | **R$ 200-500** |

---

## Modelo de cobrança para lojas

Você pode oferecer diferentes planos:

| Plano | Taxa | Para quem |
|-------|------|-----------|
| **Básico** | 5% por entrega | Lojas pequenas |
| **Intermediário** | 4% + R$ 99/mês | Lojas médias (volume > 300 entregas/mês) |
| **Premium** | 3% + R$ 199/mês | Lojas grandes (volume > 1000 entregas/mês) |

> Hoje o sistema usa taxa fixa de 5%. Para implementar planos, adicionar um campo `taxa_plataforma` na tabela `lojas`.

---

## Comparação com concorrentes

| Plataforma | Taxa total | Modelo |
|-----------|-----------|--------|
| **Delivery App (seu)** | 5% + 2,5% gateway | Marketplace com maquininha na entrega |
| iFood | 12-27% | Marketplace + antecipação |
| Rappi | 15-30% | Marketplace |
| 99Food | 12-20% | Marketplace |
| Aiqfome | 12-18% | Marketplace |

**Diferencial:** taxa significativamente menor (5% vs 12-27%), porque:
- Sem custo de marketing/aquisição de cliente (a loja traz o cliente)
- Pagamento na entrega (sem risco de chargeback para a loja)
- Operação local (sem estrutura nacional)

