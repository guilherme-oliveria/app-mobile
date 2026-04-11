package com.delivery.entrega.service;

import com.delivery.entrega.entity.Entrega;
import com.delivery.entrega.repository.EntregaRepository;
import com.delivery.motoboy.entity.Motoboy;
import com.delivery.motoboy.service.MotoboyService;
import com.delivery.pedido.entity.Pedido;
import com.delivery.pedido.service.PedidoService;
import com.delivery.shared.Enums.StatusEntrega;
import com.delivery.shared.Enums.StatusMotoboy;
import com.delivery.shared.Enums.StatusPedido;
import com.delivery.shared.SecurityUtils;
import com.delivery.shared.event.EntregaEvent.*;
import com.delivery.shared.event.EntregaEventPublisher;
import com.delivery.shared.exception.BusinessException;
import com.delivery.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final PedidoService pedidoService;
    private final MotoboyService motoboyService;
    private final SecurityUtils securityUtils;
    private final EntregaEventPublisher eventPublisher;

    // ── Consultas ────────────────────────────────────────────

    public List<Entrega> listarPorMotoboy(Long motoboyId) {
        return entregaRepository.findByMotoboyIdOrderByDataDesc(motoboyId);
    }

    /**
     * Lista entregas com status DISPONIVEL (nenhum motoboy aceitou ainda).
     * Usada pelo:
     *  - Dashboard admin → para atribuir manualmente se necessário
     *  - App motoboy → para ver e aceitar entregas disponíveis
     */
    public List<Entrega> listarDisponiveis() {
        return entregaRepository.findDisponiveis();
    }

    // ── Criar entrega (automático ao criar pedido) ───────────

    /**
     * Cria entrega com status DISPONIVEL e publica evento no RabbitMQ.
     *
     * FLUXO:
     * 1. Salva entrega no banco (rápido, ~5ms)
     * 2. Publica evento na fila (rápido, ~2ms)
     * 3. Consumer processa em background:
     *    - Envia push FCM para todos os motoboys disponíveis
     *    - Notifica admin via WebSocket
     *
     * SEM FILA: este método levaria ~3s (esperando FCM de cada motoboy).
     * COM FILA: responde em ~10ms, notificações vão em background.
     */
    @Transactional
    public Entrega criarParaPedido(Long pedidoId) {
        var pedido = pedidoService.buscarEntidade(pedidoId);

        // Verifica se já existe entrega para esse pedido
        entregaRepository.findByPedidoId(pedidoId).ifPresent(e -> {
            throw new BusinessException("Já existe entrega para o pedido #" + pedidoId);
        });

        var entrega = Entrega.builder()
                .pedido(pedido)
                .status(StatusEntrega.DISPONIVEL)
                .build();
        entrega = entregaRepository.save(entrega);

        // Publica evento na fila — notificações vão em background
        eventPublisher.publicarEntregaCriada(new EntregaCriadaEvent(
                entrega.getId(),
                pedido.getId(),
                pedido.getClienteNome(),
                pedido.getEnderecoEntrega(),
                pedido.getValorTotal(),
                pedido.getLoja().getNome()
        ));

        return entrega;
    }

    // ── Motoboy aceita entrega (auto-atribuição) ─────────────

    /**
     * Motoboy clica "ACEITAR" no app → auto-atribui a entrega.
     *
     * RACE CONDITION resolvida com @Version (optimistic locking):
     * - Dois motoboys clicam "ACEITAR" ao mesmo tempo
     * - O primeiro salva com version=0
     * - O segundo tenta salvar com version=0, mas já é version=1
     * - JPA lança ObjectOptimisticLockingFailureException
     * - GlobalExceptionHandler retorna 409 Conflict: "Entrega já aceita por outro motoboy"
     *
     * GARGALO RESOLVIDO com fila:
     * - Após aceitar, publica evento na fila (notifica admin + remove da lista)
     * - Motoboy recebe resposta em ~80ms em vez de ~2s
     */
    @Transactional
    public Entrega aceitarEntrega(Long entregaId) {
        var entrega = buscarEntidade(entregaId);

        if (entrega.getStatus() != StatusEntrega.DISPONIVEL) {
            throw new BusinessException("Entrega não está mais disponível (status: " + entrega.getStatus() + ")");
        }

        // Identifica motoboy logado via JWT
        Long motoboyId = securityUtils.getRefIdDoUsuarioLogado();
        var motoboy = motoboyService.buscarEntidade(motoboyId);

        if (motoboy.getStatus() != StatusMotoboy.DISPONIVEL) {
            throw new BusinessException("Você já está em uma entrega ativa");
        }

        // Atribui (optimistic lock protege de aceite duplo)
        atribuirEntrega(entrega, motoboy);

        // Publica evento na fila — notificações em background
        eventPublisher.publicarEntregaAceita(new EntregaAceitaEvent(
                entrega.getId(),
                motoboy.getId(),
                motoboy.getNome(),
                entrega.getPedido().getClienteNome(),
                entrega.getPedido().getEnderecoEntrega(),
                true  // autoAceite = true (motoboy aceitou sozinho)
        ));

        return entrega;
    }

    // ── Admin atribui manualmente (intervenção) ──────────────

    /**
     * Admin escolhe motoboy no dashboard e atribui.
     * Útil quando nenhum motoboy aceitou, ou para reatribuir.
     */
    @Transactional
    public Entrega atribuirMotoboy(Long entregaId, Long motoboyId) {
        var entrega = buscarEntidade(entregaId);

        if (entrega.getStatus() != StatusEntrega.DISPONIVEL) {
            throw new BusinessException("Entrega não está disponível para atribuição (status: " + entrega.getStatus() + ")");
        }

        var motoboy = motoboyService.buscarEntidade(motoboyId);

        if (motoboy.getStatus() != StatusMotoboy.DISPONIVEL) {
            throw new BusinessException("Motoboy " + motoboy.getNome() + " não está disponível");
        }

        atribuirEntrega(entrega, motoboy);

        // Publica evento na fila — FCM para motoboy + WebSocket admin
        eventPublisher.publicarEntregaAceita(new EntregaAceitaEvent(
                entrega.getId(),
                motoboy.getId(),
                motoboy.getNome(),
                entrega.getPedido().getClienteNome(),
                entrega.getPedido().getEnderecoEntrega(),
                false  // autoAceite = false (admin atribuiu)
        ));

        return entrega;
    }

    // ── Confirmar coleta ─────────────────────────────────────

    @Transactional
    public Entrega confirmarColeta(Long entregaId) {
        var entrega = buscarEntidade(entregaId);

        if (entrega.getStatus() != StatusEntrega.ATRIBUIDA) {
            throw new BusinessException("Entrega precisa estar ATRIBUIDA para coletar (status: " + entrega.getStatus() + ")");
        }

        entrega.setStatus(StatusEntrega.COLETADA);
        entrega.setColetadaEm(LocalDateTime.now());
        pedidoService.atualizarStatus(entrega.getPedido().getId(), StatusPedido.COLETADO);
        entrega = entregaRepository.save(entrega);

        // Publica mudança de status → dashboard admin atualiza em tempo real
        eventPublisher.publicarStatusAlterado(new EntregaStatusEvent(
                entrega.getId(),
                entrega.getPedido().getId(),
                entrega.getStatus().name(),
                entrega.getMotoboy().getNome(),
                entrega.getPedido().getClienteNome()
        ));

        return entrega;
    }

    // ── Finalizar entrega ────────────────────────────────────

    @Transactional
    public Entrega finalizarEntrega(Long entregaId, String codigoConfirmacao) {
        var entrega = buscarEntidade(entregaId);

        if (entrega.getStatus() != StatusEntrega.COLETADA) {
            throw new BusinessException("Entrega precisa estar COLETADA para finalizar (status: " + entrega.getStatus() + ")");
        }

        entrega.setStatus(StatusEntrega.FINALIZADA);
        entrega.setFinalizadaEm(LocalDateTime.now());
        entrega.setCodigoConfirmacao(codigoConfirmacao);
        pedidoService.atualizarStatus(entrega.getPedido().getId(), StatusPedido.ENTREGUE);
        // Libera o motoboy para próxima entrega
        motoboyService.atualizarStatus(entrega.getMotoboy().getId(), StatusMotoboy.DISPONIVEL);
        entrega = entregaRepository.save(entrega);

        // Publica mudança de status
        eventPublisher.publicarStatusAlterado(new EntregaStatusEvent(
                entrega.getId(),
                entrega.getPedido().getId(),
                entrega.getStatus().name(),
                entrega.getMotoboy().getNome(),
                entrega.getPedido().getClienteNome()
        ));

        return entrega;
    }

    // ── Timeout: entregas sem aceite há mais de 10 min ───────

    /**
     * Job agendado: roda a cada 2 minutos.
     * Detecta entregas DISPONÍVEIS que ninguém aceitou há mais de 10 minutos.
     * Notifica admin via WebSocket para que ele atribua manualmente.
     *
     * POR QUE É IMPORTANTE:
     * - Sem isso, entrega pode ficar parada para sempre se nenhum motoboy aceitar
     * - Admin recebe alerta e pode intervir (atribuir manualmente ou ligar para motoboy)
     */
    @Scheduled(fixedRate = 120_000) // a cada 2 minutos
    @Transactional(readOnly = true)
    public void verificarEntregasSemAceite() {
        var limite = LocalDateTime.now().minusMinutes(10);
        var entregasTimeout = entregaRepository.findDisponiveisAntesDe(limite);

        if (!entregasTimeout.isEmpty()) {
            log.warn("⚠️ {} entregas sem aceite há mais de 10 minutos!", entregasTimeout.size());

            for (var entrega : entregasTimeout) {
                eventPublisher.publicarStatusAlterado(new EntregaStatusEvent(
                        entrega.getId(),
                        entrega.getPedido().getId(),
                        "TIMEOUT_SEM_ACEITE",
                        null,
                        entrega.getPedido().getClienteNome()
                ));
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    /**
     * Lógica compartilhada de atribuição (usada por aceitarEntrega e atribuirMotoboy).
     */
    private void atribuirEntrega(Entrega entrega, Motoboy motoboy) {
        entrega.setMotoboy(motoboy);
        entrega.setStatus(StatusEntrega.ATRIBUIDA);
        entrega.setAtribuidaEm(LocalDateTime.now());
        motoboyService.atualizarStatus(motoboy.getId(), StatusMotoboy.EM_ENTREGA);
        pedidoService.atualizarStatus(entrega.getPedido().getId(), StatusPedido.ACEITO);
        entregaRepository.save(entrega);
    }

    public Entrega buscarEntidade(Long id) {
        return entregaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entrega não encontrada: " + id));
    }

    public String gerarCodigoConfirmacao() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
