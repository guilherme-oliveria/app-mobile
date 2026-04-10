package com.delivery.pagamento.entity;

import com.delivery.entrega.entity.Entrega;
import com.delivery.shared.Enums.StatusTransacao;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrega_id", nullable = false, unique = true)
    private Entrega entrega;

    @Column(name = "valor_bruto", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorBruto;

    @Column(name = "taxa_plataforma", precision = 10, scale = 2)
    private BigDecimal taxaPlataforma;

    @Column(name = "taxa_gateway", precision = 10, scale = 2)
    private BigDecimal taxaGateway;

    @Column(name = "valor_liquido_loja", precision = 10, scale = 2)
    private BigDecimal valorLiquidoLoja;

    // ID da transação no Pagar.me
    @Column(name = "pagarme_transaction_id")
    private String pagarmeTransactionId;

    // Serial da maquininha que processou
    @Column(name = "smart_pos_serial")
    private String smartPosSerial;

    @Enumerated(EnumType.STRING)
    private StatusTransacao status = StatusTransacao.PENDENTE;

    // Flag: já incluída na liquidação diária
    private boolean liquidada = false;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "processada_em")
    private LocalDateTime processadaEm;
}
