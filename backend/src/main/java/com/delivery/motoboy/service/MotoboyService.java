package com.delivery.motoboy.service;

import com.delivery.auth.entity.Usuario;
import com.delivery.auth.repository.UsuarioRepository;
import com.delivery.motoboy.dto.*;
import com.delivery.motoboy.entity.Motoboy;
import com.delivery.motoboy.repository.MotoboyRepository;
import com.delivery.shared.Enums.RoleUsuario;
import com.delivery.shared.Enums.StatusMotoboy;
import com.delivery.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MotoboyService {

    private final MotoboyRepository motoboyRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<MotoboyResponse> listarTodos() {
        return motoboyRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<MotoboyResponse> listarDisponiveis() {
        return motoboyRepository.findByStatusAndAtivoTrue(StatusMotoboy.DISPONIVEL)
                .stream().map(this::toResponse).toList();
    }

    public MotoboyResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public MotoboyResponse criar(MotoboyRequest request) {
        // Verifica se já existe usuário com este email
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Já existe um usuário com o email: " + request.email());
        }

        var motoboy = Motoboy.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .email(request.email())
                .telefone(request.telefone())
                .cnh(request.cnh())
                .smartPosSerial(request.smartPosSerial())
                .build();
        motoboy = motoboyRepository.save(motoboy);

        // ✅ Cria o usuário de acesso automaticamente
        var usuario = Usuario.builder()
                .email(request.email())
                .nome(request.nome())
                .senha(passwordEncoder.encode(request.senhaInicial()))
                .role(RoleUsuario.MOTOBOY)
                .refId(motoboy.getId())
                .deveAlterarSenha(true)
                .build();
        usuarioRepository.save(usuario);

        return toResponse(motoboy);
    }

    @Transactional
    public void atualizarLocalizacao(Long id, LocalizacaoRequest req) {
        var motoboy = buscarEntidade(id);
        motoboy.setLatitudeAtual(req.latitude());
        motoboy.setLongitudeAtual(req.longitude());
        motoboyRepository.save(motoboy);
    }

    @Transactional
    public void atualizarStatus(Long id, StatusMotoboy status) {
        var motoboy = buscarEntidade(id);
        motoboy.setStatus(status);
        motoboyRepository.save(motoboy);
    }

    @Transactional
    public void inativar(Long id) {
        var motoboy = buscarEntidade(id);
        if (motoboy.getStatus() == StatusMotoboy.EM_ENTREGA) {
            throw new com.delivery.shared.exception.BusinessException(
                    "Motoboy está em entrega ativa — não pode ser inativado agora");
        }
        motoboy.setAtivo(false);
        motoboy.setStatus(StatusMotoboy.INATIVO);
        motoboyRepository.save(motoboy);
    }

    @Transactional
    public void atualizarFcmToken(Long id, String token) {
        var motoboy = buscarEntidade(id);
        motoboy.setFcmToken(token);
        motoboyRepository.save(motoboy);
    }

    public Motoboy buscarEntidade(Long id) {
        return motoboyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motoboy não encontrado: " + id));
    }

    private MotoboyResponse toResponse(Motoboy m) {
        return new MotoboyResponse(
                m.getId(), m.getNome(), m.getCpf(), m.getEmail(),
                m.getTelefone(), m.getSmartPosSerial(), m.getStatus(),
                m.getLatitudeAtual(), m.getLongitudeAtual(), m.isAtivo()
        );
    }
}
