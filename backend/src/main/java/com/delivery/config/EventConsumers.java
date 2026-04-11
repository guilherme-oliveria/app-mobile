package com.delivery.config;

import com.delivery.motoboy.entity.Motoboy;
import com.delivery.motoboy.repository.MotoboyRepository;
import com.delivery.shared.Enums.StatusMotoboy;
import com.delivery.shared.event.EntregaEvent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Consumers do RabbitMQ — processam eventos em background.
 *
 * BENEFÍCIOS:
 * - O endpoint HTTP responde imediato (não espera FCM/WebSocket)
 * - Se Firebase cair, a mensagem fica na fila e é reprocessada
 * - Escala horizontal: pode subir N consumers para processar mais rápido
 * - Desacoplamento: EntregaService não conhece FCM nem WebSocket
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumers {

    private final FirebaseNotificacaoService fcmService;
    private final SimpMessagingTemplate webSocket;
    private final MotoboyRepository motoboyRepository;

    /**
     * CONSUMER 1: Notificação push individual.
     * Fila: fila.notificacao
     * Consome eventos de push FCM para um motoboy específico.
     */
    @RabbitListener(queues = RabbitMQConfig.FILA_NOTIFICACAO)
    public void processarNotificacaoPush(NotificacaoPushEvent event) {
        log.info("Processando notificação push: {}", event.titulo());
        fcmService.enviarParaMotoboy(event.fcmToken(), event.titulo(), event.corpo());
    }

    /**
     * CONSUMER 2: Entrega criada → notifica motoboys + dashboard admin.
     * Fila: fila.entrega.criada
     *
     * GARGALO RESOLVIDO: quando uma loja cria um pedido, precisamos:
     * 1. Salvar no banco (síncrono, rápido)
     * 2. Notificar TODOS os motoboys disponíveis via FCM (lento, I/O externo)
     * 3. Atualizar dashboard do admin via WebSocket
     *
     * Sem fila: o POST /pedidos levaria 2-5s esperando FCM responder.
     * Com fila: POST /pedidos responde em ~50ms, notificações vão em background.
     */
    @RabbitListener(queues = RabbitMQConfig.FILA_ENTREGA_CRIADA)
    public void processarEntregaCriada(EntregaCriadaEvent event) {
        log.info("Processando ENTREGA_CRIADA: entregaId={}", event.entregaId());

        // 1. Notifica admin via WebSocket (dashboard atualiza em tempo real)
        webSocket.convertAndSend("/topic/entregas", Map.of(
                "tipo", "NOVA_ENTREGA",
                "entregaId", event.entregaId(),
                "pedidoId", event.pedidoId(),
                "clienteNome", event.clienteNome(),
                "endereco", event.enderecoEntrega(),
                "valor", event.valorTotal(),
                "loja", event.lojaNome()
        ));

        // 2. Notifica TODOS os motoboys disponíveis via FCM
        List<Motoboy> motoboysDisponiveis = motoboyRepository
                .findByStatusAndAtivoTrue(StatusMotoboy.DISPONIVEL);

        for (Motoboy motoboy : motoboysDisponiveis) {
            if (motoboy.getFcmToken() != null && !motoboy.getFcmToken().isBlank()) {
                fcmService.enviarParaMotoboy(
                        motoboy.getFcmToken(),
                        "🔔 Nova entrega disponível!",
                        String.format("%s → %s | R$ %.2f",
                                event.lojaNome(), event.enderecoEntrega(), event.valorTotal())
                );
            }
        }

        log.info("Notificados {} motoboys disponíveis sobre entrega #{}", 
                motoboysDisponiveis.size(), event.entregaId());
    }

    /**
     * CONSUMER 3: Entrega aceita → notifica admin + motoboy.
     * Fila: fila.entrega.aceita
     *
     * GARGALO RESOLVIDO: quando motoboy aceita, o PATCH /entregas/{id}/aceitar precisa:
     * 1. Atribuir no banco (síncrono, com lock otimista)
     * 2. Notificar admin via WebSocket (entrega saiu da lista de disponíveis)
     * 3. Notificar motoboy atribuído via FCM (confirmação)
     *
     * Sem fila: motoboy espera ~2s para WebSocket + FCM responderem.
     * Com fila: motoboy recebe resposta em ~80ms.
     */
    @RabbitListener(queues = RabbitMQConfig.FILA_ENTREGA_ACEITA)
    public void processarEntregaAceita(EntregaAceitaEvent event) {
        log.info("Processando ENTREGA_ACEITA: entregaId={}, motoboy={}",
                event.entregaId(), event.motoboyNome());

        // 1. Notifica admin via WebSocket
        webSocket.convertAndSend("/topic/entregas", Map.of(
                "tipo", "ENTREGA_ACEITA",
                "entregaId", event.entregaId(),
                "motoboyId", event.motoboyId(),
                "motoboyNome", event.motoboyNome(),
                "clienteNome", event.clienteNome(),
                "autoAceite", event.autoAceite()
        ));

        // 2. Se admin atribuiu manualmente, notifica motoboy via FCM
        if (!event.autoAceite()) {
            var motoboy = motoboyRepository.findById(event.motoboyId());
            motoboy.ifPresent(m -> {
                if (m.getFcmToken() != null) {
                    fcmService.enviarParaMotoboy(
                            m.getFcmToken(),
                            "📦 Entrega atribuída a você!",
                            String.format("Cliente: %s | %s",
                                    event.clienteNome(), event.enderecoEntrega())
                    );
                }
            });
        }
    }

    /**
     * CONSUMER 4: Mudança de status → atualiza dashboard admin.
     * Fila: fila.entrega.status
     * Usado em: coleta confirmada, entrega finalizada, cancelamento.
     */
    @RabbitListener(queues = RabbitMQConfig.FILA_ENTREGA_STATUS)
    public void processarStatusAlterado(EntregaStatusEvent event) {
        log.info("Processando STATUS_ALTERADO: entregaId={}, status={}",
                event.entregaId(), event.status());

        webSocket.convertAndSend("/topic/entregas", Map.of(
                "tipo", "STATUS_ALTERADO",
                "entregaId", event.entregaId(),
                "pedidoId", event.pedidoId(),
                "status", event.status(),
                "motoboyNome", event.motoboyNome() != null ? event.motoboyNome() : "",
                "clienteNome", event.clienteNome()
        ));
    }
}

