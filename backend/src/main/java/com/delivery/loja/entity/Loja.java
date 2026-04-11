package com.delivery.loja.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lojas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Loja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefone;
    private String endereco;

    @Column(name = "chave_pix")
    private String chavePix;

    // ID do recebedor no Pagar.me (gerado no onboarding)
    @Column(name = "pagarme_recipient_id")
    private String pagarmeRecipientId;

    // Saldo a liquidar (calculado no fim do dia)
    @Column(name = "saldo_pendente", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal saldoPendente = BigDecimal.ZERO;

    @Builder.Default
    private boolean ativo = true;

    @Column(name = "criado_em")
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "atualizado_em")
    @Builder.Default
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
