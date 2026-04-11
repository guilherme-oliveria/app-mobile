package com.delivery.shared.event;

import com.delivery.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos no RabbitMQ.
 * Chamado pelos services (EntregaService, PedidoService) após operações de negócio.
 *
 * POR QUE USAR FILA AQUI:
 * - Notificação FCM é I/O externo → se cair, não trava o fluxo principal
 * - WebSocket para admin é best-effort → se admin não está online, não importa
 * - Retry automático: se o consumer falhar, a mensagem volta para a fila
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntregaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica evento de entrega criada.
     * Consumers vão: notificar motoboys via FCM + atualizar dashboard admin via WebSocket.
     */
    public void publicarEntregaCriada(EntregaEvent.EntregaCriadaEvent event) {
        log.info("Publicando evento ENTREGA_CRIADA: entregaId={}", event.entregaId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DELIVERY,
                RabbitMQConfig.RK_ENTREGA_CRIADA,
                event
        );
    }

    /**
     * Publica evento de entrega aceita (por motoboy ou admin).
     * Consumers vão: atualizar dashboard + notificar motoboy atribuído.
     */
    public void publicarEntregaAceita(EntregaEvent.EntregaAceitaEvent event) {
        log.info("Publicando evento ENTREGA_ACEITA: entregaId={}, motoboyId={}, autoAceite={}",
                event.entregaId(), event.motoboyId(), event.autoAceite());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DELIVERY,
                RabbitMQConfig.RK_ENTREGA_ACEITA,
                event
        );
    }

    /**
     * Publica mudança de status da entrega.
     * Consumer: atualiza dashboard do admin em tempo real.
     */
    public void publicarStatusAlterado(EntregaEvent.EntregaStatusEvent event) {
        log.info("Publicando evento ENTREGA_STATUS: entregaId={}, status={}",
                event.entregaId(), event.status());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DELIVERY,
                RabbitMQConfig.RK_ENTREGA_STATUS,
                event
        );
    }

    /**
     * Publica notificação push para um motoboy específico.
     */
    public void publicarNotificacaoPush(EntregaEvent.NotificacaoPushEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DELIVERY,
                RabbitMQConfig.RK_NOTIFICACAO,
                event
        );
    }
}

