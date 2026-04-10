package com.delivery.auth.controller;

import com.delivery.auth.entity.Usuario;
import com.delivery.auth.repository.UsuarioRepository;
import com.delivery.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        Usuario usuario = usuarioRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.gerarToken(userDetails, usuario.getRole().name(), usuario.getRefId());

        return ResponseEntity.ok(new LoginResponse(token, usuario.getRole().name(), usuario.getNome()));
    }

    // Records como DTOs (Java 21)
    public record LoginRequest(String email, String senha) {}
    public record LoginResponse(String token, String role, String nome) {}
}
