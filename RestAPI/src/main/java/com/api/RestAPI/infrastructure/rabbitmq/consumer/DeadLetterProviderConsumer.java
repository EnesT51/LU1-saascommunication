package com.api.RestAPI.infrastructure.rabbitmq.consumer;

import com.api.RestAPI.application.messageprovider.service.ProviderMessageStatusService;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterProviderConsumer {

    private final ProviderMessageStatusService statusService;

    public DeadLetterProviderConsumer(ProviderMessageStatusService statusService) {
        this.statusService = statusService;
    }

    @RabbitListener(queues = {
            RabbitMQConfig.SWIFTSEND_DLQ,
            RabbitMQConfig.SECUREPOST_DLQ,
            RabbitMQConfig.LEGACYLINK_DLQ,
            RabbitMQConfig.ASYNCFLOW_DLQ
    })
    public void consumeDeadLetter(ProviderMessage message) {
        statusService.markAsDeadLetter(
                message.getId(),
                "Message moved to dead letter queue"
        );
    }
}