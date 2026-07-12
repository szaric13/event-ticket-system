package com.example.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "booking.exchange";
    public static final String CONFIRMED_QUEUE = "booking.confirmed";
    public static final String EXPIRED_QUEUE = "booking.expired";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue confirmedQueue() {
        return new Queue(CONFIRMED_QUEUE, true);
    }

    @Bean
    public Queue expiredQueue() {
        return new Queue(EXPIRED_QUEUE, true);
    }

    @Bean
    public Binding confirmedBinding(Queue confirmedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(confirmedQueue).to(exchange).with("booking.confirmed");
    }

    @Bean
    public Binding expiredBinding(Queue expiredQueue, DirectExchange exchange) {
        return BindingBuilder.bind(expiredQueue).to(exchange).with("booking.expired");
    }
}