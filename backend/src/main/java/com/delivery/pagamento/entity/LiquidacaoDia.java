package com.delivery.pagamento.entity;

import com.delivery.loja.entity.Loja;
import com.delivery.shared.Enums.StatusLiquidacao;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidacoes_diarias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiquidacaoDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Column(name = "total_entregas")
    private Integer totalEntregas;

    @Column(name = "valor_bruto", precision = 10, scale = 2)
    private BigDecimal valorBruto;

    @Column(name = "total_taxas", precision = 10, scale = 2)
    private BigDecimal totalTaxas;

    @Column(name = "valor_liquido", precision = 10, scale = 2)
    private BigDecimal valorLiquido;

    // ID da transferência no Pagar.me
    @Column(name = "pagarme_transfer_id")
    private String pagarmeTransferId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusLiquidacao status = StatusLiquidacao.PENDENTE;

    @Column(name = "processada_em")
    private LocalDateTime processadaEm;
}
