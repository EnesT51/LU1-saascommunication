package com.api.RestAPI.application.messageprovider.service;

import com.api.RestAPI.application.messageprovider.interfaces.MessageProviderUseCase;
import com.api.RestAPI.domain.message.interfaces.MessageQueuePublisher;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import org.springframework.stereotype.Service;

@Service
public class MessageProviderService implements MessageProviderUseCase {

    private final MessageQueuePublisher messageQueuePublisher;

    public MessageProviderService(MessageQueuePublisher messageQueuePublisher) {
        this.messageQueuePublisher = messageQueuePublisher;
    }

    @Override
    public void queueMessage(ProviderMessage message) {
        messageQueuePublisher.publish(message);
    }
}