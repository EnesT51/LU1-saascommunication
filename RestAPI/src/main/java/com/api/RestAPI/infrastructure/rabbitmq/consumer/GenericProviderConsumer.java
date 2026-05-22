package com.api.RestAPI.infrastructure.rabbitmq.consumer;

import com.api.RestAPI.application.messageprovider.service.ProviderDispatcher;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GenericProviderConsumer {

    private final ProviderDispatcher providerDispatcher;

    public GenericProviderConsumer(ProviderDispatcher providerDispatcher) {
        this.providerDispatcher = providerDispatcher;
    }

    @RabbitListener(queues = {
            "swiftsend.queue",
            "securepost.queue",
            "legacylink.queue",
            "asyncflow.queue"
    })
    public void consume(ProviderMessage message) {
        providerDispatcher.dispatch(message);
    }
}