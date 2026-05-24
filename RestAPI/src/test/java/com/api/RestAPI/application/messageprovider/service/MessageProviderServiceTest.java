package com.api.RestAPI.application.messageprovider.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.domain.message.enums.MessageStatus;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageQueuePublisher;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.persistence.message.entity.ProviderMessageEntity;
import com.api.RestAPI.infrastructure.persistence.message.repository.ProviderMessageJpaRepository;

@DisplayName("MessageProviderService Tests")
class MessageProviderServiceTest {

    @Test
    @DisplayName("Should save entity and publish message to queue")
    void queueMessageSavesAndPublishes() {
        // Arrange
        MessageQueuePublisher publisher = mock(MessageQueuePublisher.class);
        ProviderMessageJpaRepository repository = mock(ProviderMessageJpaRepository.class);

        ProviderMessageEntity savedEntity = new ProviderMessageEntity(
                ProviderType.SWIFTSEND, "recipient", "content", "subject", MessageStatus.QUEUED);

        when(repository.save(any())).thenReturn(savedEntity);

        MessageProviderService service = new MessageProviderService(publisher, repository);
        ProviderMessage message = new ProviderMessage(
                ProviderType.SWIFTSEND, "recipient", "content", "subject", null);

        // Act
        service.queueMessage(message);

        // Assert
        verify(repository).save(any());
        verify(publisher).publish(any(ProviderMessage.class));
    }
}
