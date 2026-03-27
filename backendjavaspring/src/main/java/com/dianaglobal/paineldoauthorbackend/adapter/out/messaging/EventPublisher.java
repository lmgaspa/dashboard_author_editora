package com.dianaglobal.paineldoauthorbackend.adapter.out.messaging;

import com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.event.AuthEmailEvent;
import com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.event.ChargeEmailEvent;
import com.dianaglobal.paineldoauthorbackend.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishChargeEvent(String routingKey, ChargeEmailEvent event) {
        log.info("[EVENT] Publishing {} to exchange {}", routingKey, RabbitMQConfig.EXCHANGE);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
    }

    public void publishAuthEvent(String routingKey, AuthEmailEvent event) {
        log.info("[EVENT] Publishing {} to exchange {}", routingKey, RabbitMQConfig.EXCHANGE);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
    }
}
