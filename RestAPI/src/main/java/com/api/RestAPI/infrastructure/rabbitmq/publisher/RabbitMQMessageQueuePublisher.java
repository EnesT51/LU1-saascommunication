package com.api.RestAPI.infrastructure.rabbitmq.publisher;

import com.api.RestAPI.domain.message.interfaces.DeadLetterMessageQueuePublisher;
import com.api.RestAPI.domain.message.interfaces.MessageQueuePublisher;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQMessageQueuePublisher implements MessageQueuePublisher, DeadLetterMessageQueuePublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQMessageQueuePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(ProviderMessage message) {
        String routingKey = getRoutingKey(message);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MESSAGE_EXCHANGE,
                routingKey,
                message
        );
    }

    @Override
    public void publishToDlq(ProviderMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATIONS_DLQ,
                message
        );
    }

    private String getRoutingKey(ProviderMessage message) {
        return switch (message.getProviderType()) {
            case SWIFTSEND -> RabbitMQConfig.SWIFTSEND_ROUTING_KEY;
            case SECUREPOST -> RabbitMQConfig.SECUREPOST_ROUTING_KEY;
            case LEGACYLINK -> RabbitMQConfig.LEGACYLINK_ROUTING_KEY;
            case ASYNCFLOW -> RabbitMQConfig.ASYNCFLOW_ROUTING_KEY;
        };
    }
}
