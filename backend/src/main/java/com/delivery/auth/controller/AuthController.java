package com.delivery.auth.controller;

import com.delivery.auth.entity.Usuario;
import com.delivery.auth.repository.UsuarioRepository;
import com.delivery.auth.service.JwtService;
import com.delivery.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        Usuario usuario = usuarioRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.gerarToken(userDetails, usuario.getRole().name(), usuario.getRefId());

        return ResponseEntity.ok(new LoginResponse(
                token, usuario.getRole().name(), usuario.getNome(),
                usuario.getRefId(), usuario.isDeveAlterarSenha()));
    }

    @PostMapping("/trocar-senha")
    public ResponseEntity<MsgResponse> trocarSenha(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TrocarSenhaRequest request) {

        if (request.novaSenha() == null || request.novaSenha().length() < 6) {
            throw new BusinessException("Nova senha deve ter no mínimo 6 caracteres");
        }

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        // Verifica se a senha atual confere
        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new BusinessException("Senha atual incorreta");
        }

        // Atualiza a senha
        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuario.setDeveAlterarSenha(false);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new MsgResponse("Senha alterada com sucesso"));
    }

    // Records como DTOs (Java 21)
    public record LoginRequest(String email, String senha) {}
    public record LoginResponse(String token, String role, String nome, Long refId, boolean deveAlterarSenha) {}
    public record TrocarSenhaRequest(String senhaAtual, String novaSenha) {}
    public record MsgResponse(String mensagem) {}
}
