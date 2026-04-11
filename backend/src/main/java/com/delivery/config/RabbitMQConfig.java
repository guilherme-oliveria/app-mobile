package com.delivery.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ — filas, exchanges e bindings.
 *
 * ONDE USAR FILA (pontos de gargalo):
 *
 * 1. NOTIFICAÇÃO FCM (fila.notificacao)
 *    → Enviar push para motoboys é I/O externo (Firebase).
 *    → Se chamar síncrono, o POST /pedidos fica lento esperando FCM responder.
 *    → Com fila: salva no banco + publica na fila → responde 201 imediato.
 *    → Consumer processa em background: se Firebase cair, retry automático.
 *
 * 2. ENTREGA CRIADA (fila.entrega.criada)
 *    → Quando pedido é criado, cria a entrega + notifica admin via WebSocket.
 *    → Desacopla: PedidoService não precisa conhecer WebSocket nem FCM.
 *
 * 3. ENTREGA ACEITA (fila.entrega.aceita)
 *    → Quando motoboy aceita, precisa: notificar admin + remover da lista dos outros motoboys.
 *    → Tudo assíncrono para responder rápido ao motoboy.
 *
 * 4. PAGAMENTO (fila.pagamento)
 *    → Processar pagamento no Pagar.me é I/O externo.
 *    → Com fila: registra intenção + processa em background.
 *    → Se Pagar.me estiver fora, retry automático (DLQ).
 */
@Configuration
public class RabbitMQConfig {

    // ── Nomes das filas ──────────────────────────────────────
    public static final String FILA_NOTIFICACAO = "fila.notificacao";
    public static final String FILA_ENTREGA_CRIADA = "fila.entrega.criada";
    public static final String FILA_ENTREGA_ACEITA = "fila.entrega.aceita";
    public static final String FILA_ENTREGA_STATUS = "fila.entrega.status";
    public static final String FILA_PAGAMENTO = "fila.pagamento";

    // ── Exchange ─────────────────────────────────────────────
    public static final String EXCHANGE_DELIVERY = "delivery.exchange";

    // ── Routing keys ─────────────────────────────────────────
    public static final String RK_NOTIFICACAO = "delivery.notificacao";
    public static final String RK_ENTREGA_CRIADA = "delivery.entrega.criada";
    public static final String RK_ENTREGA_ACEITA = "delivery.entrega.aceita";
    public static final String RK_ENTREGA_STATUS = "delivery.entrega.status";
    public static final String RK_PAGAMENTO = "delivery.pagamento";

    // ── Exchange ─────────────────────────────────────────────
    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(EXCHANGE_DELIVERY);
    }

    // ── Filas ────────────────────────────────────────────────
    @Bean
    public Queue filaNotificacao() {
        return QueueBuilder.durable(FILA_NOTIFICACAO).build();
    }

    @Bean
    public Queue filaEntregaCriada() {
        return QueueBuilder.durable(FILA_ENTREGA_CRIADA).build();
    }

    @Bean
    public Queue filaEntregaAceita() {
        return QueueBuilder.durable(FILA_ENTREGA_ACEITA).build();
    }

    @Bean
    public Queue filaEntregaStatus() {
        return QueueBuilder.durable(FILA_ENTREGA_STATUS).build();
    }

    @Bean
    public Queue filaPagamento() {
        return QueueBuilder.durable(FILA_PAGAMENTO).build();
    }

    // ── Bindings ─────────────────────────────────────────────
    @Bean
    public Binding bindingNotificacao() {
        return BindingBuilder.bind(filaNotificacao()).to(deliveryExchange()).with(RK_NOTIFICACAO);
    }

    @Bean
    public Binding bindingEntregaCriada() {
        return BindingBuilder.bind(filaEntregaCriada()).to(deliveryExchange()).with(RK_ENTREGA_CRIADA);
    }

    @Bean
    public Binding bindingEntregaAceita() {
        return BindingBuilder.bind(filaEntregaAceita()).to(deliveryExchange()).with(RK_ENTREGA_ACEITA);
    }

    @Bean
    public Binding bindingEntregaStatus() {
        return BindingBuilder.bind(filaEntregaStatus()).to(deliveryExchange()).with(RK_ENTREGA_STATUS);
    }

    @Bean
    public Binding bindingPagamento() {
        return BindingBuilder.bind(filaPagamento()).to(deliveryExchange()).with(RK_PAGAMENTO);
    }

    // ── Serialização JSON ────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

