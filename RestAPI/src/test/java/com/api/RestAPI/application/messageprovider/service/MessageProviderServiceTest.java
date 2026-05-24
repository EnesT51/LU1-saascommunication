package com.api.RestAPI.application.messageprovider.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageQueuePublisher;
import com.api.RestAPI.domain.message.model.ProviderMessage;

@DisplayName("MessageProviderService Tests")
class MessageProviderServiceTest {

    @Test
    @DisplayName("Should publish message to queue")
    void queueMessagePublishesMessage() {
        MessageQueuePublisher publisher = mock(MessageQueuePublisher.class);
        MessageProviderService service = new MessageProviderService(publisher);
        ProviderMessage message = new ProviderMessage(ProviderType.SWIFTSEND, "recipient", "content", "subject");

        service.queueMessage(message);

        verify(publisher).publish(message);
    }
}
