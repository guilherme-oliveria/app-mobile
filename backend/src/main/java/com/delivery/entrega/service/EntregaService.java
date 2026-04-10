package com.delivery.entrega.service;

import com.delivery.entrega.entity.Entrega;
import com.delivery.entrega.repository.EntregaRepository;
import com.delivery.motoboy.service.MotoboyService;
import com.delivery.pedido.service.PedidoService;
import com.delivery.shared.Enums.StatusEntrega;
import com.delivery.shared.Enums.StatusMotoboy;
import com.delivery.shared.Enums.StatusPedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final PedidoService pedidoService;
    private final MotoboyService motoboyService;

    public List<Entrega> listarPorMotoboy(Long motoboyId) {
        return entregaRepository.findByMotoboyIdOrderByDataDesc(motoboyId);
    }

    public List<Entrega> listarPendentes() {
        return entregaRepository.findByStatus(StatusEntrega.PENDENTE);
    }

    @Transactional
    public Entrega criarParaPedido(Long pedidoId) {
        var pedido = pedidoService.buscarEntidade(pedidoId);
        var entrega = Entrega.builder()
                .pedido(pedido)
                .status(StatusEntrega.PENDENTE)
                .build();
        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega atribuirMotoboy(Long entregaId, Long motoboyId) {
        var entrega = buscarEntidade(entregaId);
        var motoboy = motoboyService.buscarEntidade(motoboyId);

        entrega.setMotoboy(motoboy);
        entrega.setStatus(StatusEntrega.ATRIBUIDA);
        entrega.setAtribuidaEm(LocalDateTime.now());

        // atualiza status do motoboy e do pedido
        motoboyService.atualizarStatus(motoboyId, StatusMotoboy.EM_ENTREGA);
        pedidoService.atualizarStatus(entrega.getPedido().getId(), StatusPedido.ACEITO);

        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega confirmarColeta(Long entregaId) {
        var entrega = buscarEntidade(entregaId);
        entrega.setStatus(StatusEntrega.COLETADA);
        entrega.setColetadaEm(LocalDateTime.now());
        pedidoService.atualizarStatus(entrega.getPedido().getId(), StatusPedido.COLETADO);
        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega finalizarEntrega(Long entregaId, String codigoConfirmacao) {
        var entrega = buscarEntidade(entregaId);
        entrega.setStatus(StatusEntrega.FINALIZADA);
        entrega.setFinalizadaEm(LocalDateTime.now());
        entrega.setCodigoConfirmacao(codigoConfirmacao);
        pedidoService.atualizarStatus(entrega.getPedido().getId(), StatusPedido.ENTREGUE);
        // libera o motoboy
        motoboyService.atualizarStatus(entrega.getMotoboy().getId(), StatusMotoboy.DISPONIVEL);
        return entregaRepository.save(entrega);
    }

    public Entrega buscarEntidade(Long id) {
        return entregaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada: " + id));
    }

    public String gerarCodigoConfirmacao() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
