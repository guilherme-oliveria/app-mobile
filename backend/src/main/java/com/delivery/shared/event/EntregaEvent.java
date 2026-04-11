package com.delivery.shared.event;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Eventos publicados no RabbitMQ.
 * Usados para desacoplar o fluxo principal de notificações e processos assíncronos.
 */
public class EntregaEvent {

    /**
     * Publicado quando uma entrega é criada (status DISPONIVEL).
     * Consumer: envia push FCM para todos os motoboys + WebSocket para admin.
     */
    public record EntregaCriadaEvent(
            Long entregaId,
            Long pedidoId,
            String clienteNome,
            String enderecoEntrega,
            BigDecimal valorTotal,
            String lojaNome
    ) implements Serializable {}

    /**
     * Publicado quando motoboy aceita ou admin atribui a entrega.
     * Consumer: notifica admin via WebSocket + push FCM para motoboy atribuído.
     */
    public record EntregaAceitaEvent(
            Long entregaId,
            Long motoboyId,
            String motoboyNome,
            String clienteNome,
            String enderecoEntrega,
            boolean autoAceite  // true = motoboy aceitou, false = admin atribuiu
    ) implements Serializable {}

    /**
     * Publicado em cada mudança de status da entrega.
     * Consumer: notifica admin via WebSocket (atualização em tempo real no dashboard).
     */
    public record EntregaStatusEvent(
            Long entregaId,
            Long pedidoId,
            String status,
            String motoboyNome,
            String clienteNome
    ) implements Serializable {}

    /**
     * Evento de notificação push (FCM).
     * Consumer: FirebaseNotificacaoService envia o push em background.
     */
    public record NotificacaoPushEvent(
            String fcmToken,
            String titulo,
            String corpo
    ) implements Serializable {}

    /**
     * Evento para notificar TODOS os motoboys disponíveis (broadcast).
     */
    public record NotificacaoBroadcastEvent(
            String titulo,
            String corpo
    ) implements Serializable {}
}

