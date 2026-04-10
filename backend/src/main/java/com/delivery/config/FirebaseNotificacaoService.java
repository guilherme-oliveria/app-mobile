package com.delivery.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Serviço de notificações push via Firebase Cloud Messaging (FCM).
 *
 * SETUP necessário:
 * 1. Crie um projeto no Firebase Console (console.firebase.google.com)
 * 2. Vá em Configurações do Projeto > Contas de serviço > Gerar nova chave privada
 * 3. Salve o JSON em src/main/resources/firebase-credentials.json
 */
@Slf4j
@Service
public class FirebaseNotificacaoService {

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    @PostConstruct
    public void inicializar() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                var credentials = GoogleCredentials.fromStream(
                        new ClassPathResource("firebase-credentials.json").getInputStream());
                var options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase inicializado com sucesso");
            }
        } catch (IOException e) {
            log.warn("Firebase não inicializado — credenciais não encontradas. " +
                     "Adicione firebase-credentials.json em src/main/resources/");
        }
    }

    /**
     * Envia notificação push para um motoboy específico.
     * @param fcmToken  token do dispositivo (salvo no campo Motoboy.fcmToken)
     */
    public void enviarParaMotoboy(String fcmToken, String titulo, String corpo) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM token não definido — notificação não enviada");
            return;
        }
        try {
            var mensagem = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(corpo)
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(mensagem);
            log.info("Notificação enviada: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Erro ao enviar notificação FCM: {}", e.getMessage());
        }
    }

    /**
     * Envia para um tópico (ex: todas as lojas de uma região).
     */
    public void enviarParaTopico(String topico, String titulo, String corpo) {
        try {
            var mensagem = Message.builder()
                    .setTopic(topico)
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(corpo)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(mensagem);
        } catch (FirebaseMessagingException e) {
            log.error("Erro ao enviar para tópico {}: {}", topico, e.getMessage());
        }
    }
}
