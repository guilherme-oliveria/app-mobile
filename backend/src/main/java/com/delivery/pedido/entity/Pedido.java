package com.delivery.pedido.entity;

import com.delivery.loja.entity.Loja;
import com.delivery.shared.Enums.StatusPedido;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    // Dados do cliente (sem cadastro necessário)
    @Column(name = "cliente_nome")
    private String clienteNome;

    @Column(name = "cliente_telefone")
    private String clienteTelefone;

    @Column(name = "endereco_entrega", nullable = false)
    private String enderecoEntrega;

    @Column(name = "latitude_entrega")
    private Double latitudeEntrega;

    @Column(name = "longitude_entrega")
    private Double longitudeEntrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "taxa_entrega", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxaEntrega = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusPedido status = StatusPedido.AGUARDANDO_ACEITE;

    private String observacao;

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
