package com.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // para liquidação automática fim do dia
public class DeliveryAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryAppApplication.class, args);
    }
}
