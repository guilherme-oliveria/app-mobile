package com.delivery.loja.service;

import com.delivery.auth.entity.Usuario;
import com.delivery.auth.repository.UsuarioRepository;
import com.delivery.loja.dto.LojaRequest;
import com.delivery.loja.dto.LojaResponse;
import com.delivery.loja.entity.Loja;
import com.delivery.loja.repository.LojaRepository;
import com.delivery.shared.Enums.RoleUsuario;
import com.delivery.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LojaService {

    private final LojaRepository lojaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<LojaResponse> listarTodas() {
        return lojaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public LojaResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public LojaResponse criar(LojaRequest request) {
        // Verifica se já existe usuário com este email
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Já existe um usuário com o email: " + request.email());
        }

        var loja = Loja.builder()
                .nome(request.nome())
                .cnpj(request.cnpj())
                .email(request.email())
                .telefone(request.telefone())
                .endereco(request.endereco())
                .chavePix(request.chavePix())
                .build();
        loja = lojaRepository.save(loja);

        // ✅ Cria o usuário de acesso automaticamente
        var usuario = Usuario.builder()
                .email(request.email())
                .nome(request.nome())
                .senha(passwordEncoder.encode(request.senhaInicial()))
                .role(RoleUsuario.LOJA)
                .refId(loja.getId())
                .deveAlterarSenha(true)
                .build();
        usuarioRepository.save(usuario);

        return toResponse(loja);
    }

    @Transactional
    public LojaResponse atualizar(Long id, LojaRequest request) {
        var loja = buscarEntidade(id);
        loja.setNome(request.nome());
        loja.setTelefone(request.telefone());
        loja.setEndereco(request.endereco());
        loja.setChavePix(request.chavePix());
        return toResponse(lojaRepository.save(loja));
    }

    @Transactional
    public void inativar(Long id) {
        var loja = buscarEntidade(id);
        loja.setAtivo(false);
        lojaRepository.save(loja);
    }

    public Loja buscarEntidade(Long id) {
        return lojaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada: " + id));
    }

    private LojaResponse toResponse(Loja loja) {
        return new LojaResponse(
                loja.getId(), loja.getNome(), loja.getCnpj(),
                loja.getEmail(), loja.getTelefone(), loja.getEndereco(),
                loja.isAtivo(), loja.getSaldoPendente()
        );
    }
}
