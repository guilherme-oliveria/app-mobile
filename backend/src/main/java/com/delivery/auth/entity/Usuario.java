package com.delivery.auth.entity;

import com.delivery.shared.Enums.RoleUsuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    private RoleUsuario role;

    // referencia para loja ou motoboy dependendo do role
    @Column(name = "ref_id")
    private Long refId;

    @Builder.Default
    private boolean ativo = true;

    @Column(name = "deve_alterar_senha")
    @Builder.Default
    private boolean deveAlterarSenha = false;

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
