package com.delivery.entrega.entity;

import com.delivery.motoboy.entity.Motoboy;
import com.delivery.pedido.entity.Pedido;
import com.delivery.shared.Enums.StatusEntrega;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motoboy_id")
    private Motoboy motoboy;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEntrega status = StatusEntrega.DISPONIVEL;

    // Optimistic locking — evita dois motoboys aceitarem ao mesmo tempo
    @Version
    private Long version;

    @Column(name = "atribuida_em")
    private LocalDateTime atribuidaEm;

    @Column(name = "coletada_em")
    private LocalDateTime coletadaEm;

    @Column(name = "finalizada_em")
    private LocalDateTime finalizadaEm;

    // Código de confirmação de entrega (assinatura digital simples)
    @Column(name = "codigo_confirmacao")
    private String codigoConfirmacao;

    @Column(name = "criado_em")
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}
