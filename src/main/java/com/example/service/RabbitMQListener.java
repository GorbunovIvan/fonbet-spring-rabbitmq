package com.example.service;

import com.example.model.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQListener {

    private final GameService gameService;

    @RabbitListener(queues = "${spring.rabbitmq.queue-parser}")
    public void processMyQueue(Message<Game> message) {
        gameService.create(message.getPayload());
    }
}
