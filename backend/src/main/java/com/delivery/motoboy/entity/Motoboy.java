package com.delivery.motoboy.entity;

import com.delivery.shared.Enums.StatusMotoboy;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "motoboys")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Motoboy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefone;
    private String cnh;

    // Serial da maquininha Smart POS atribuída
    @Column(name = "smart_pos_serial", unique = true)
    private String smartPosSerial;

    // Token Firebase para push notifications
    @Column(name = "fcm_token")
    private String fcmToken;

    // Localização em tempo real (atualizada via app)
    @Column(name = "latitude_atual")
    private Double latitudeAtual;

    @Column(name = "longitude_atual")
    private Double longitudeAtual;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusMotoboy status = StatusMotoboy.DISPONIVEL;

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
