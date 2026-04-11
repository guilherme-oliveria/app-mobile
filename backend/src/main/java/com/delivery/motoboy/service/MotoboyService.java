package com.delivery.motoboy.service;

import com.delivery.motoboy.dto.*;
import com.delivery.motoboy.entity.Motoboy;
import com.delivery.motoboy.repository.MotoboyRepository;
import com.delivery.shared.Enums.StatusMotoboy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MotoboyService {

    private final MotoboyRepository motoboyRepository;

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
        var motoboy = Motoboy.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .email(request.email())
                .telefone(request.telefone())
                .cnh(request.cnh())
                .smartPosSerial(request.smartPosSerial())
                .build();
        return toResponse(motoboyRepository.save(motoboy));
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
