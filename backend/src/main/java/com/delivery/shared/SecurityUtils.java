package com.delivery.shared;

import com.delivery.auth.entity.Usuario;
import com.delivery.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilitário para extrair informações do usuário logado via SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    /**
     * Retorna o refId (ID da loja ou motoboy) do usuário autenticado.
     */
    public Long getRefIdDoUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
        return usuario.getRefId();
    }

    /**
     * Retorna o email do usuário autenticado.
     */
    public String getEmailDoUsuarioLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

