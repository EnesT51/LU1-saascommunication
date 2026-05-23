package com.api.RestAPI.application.messageprovider.service;

import com.api.RestAPI.application.messageprovider.interfaces.MessageProviderUseCase;
import com.api.RestAPI.domain.message.enums.MessageStatus;
import com.api.RestAPI.domain.message.interfaces.MessageQueuePublisher;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.persistence.message.entity.ProviderMessageEntity;
import com.api.RestAPI.infrastructure.persistence.message.mapper.ProviderMessageMapper;
import com.api.RestAPI.infrastructure.persistence.message.repository.ProviderMessageJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageProviderService implements MessageProviderUseCase {

    private final MessageQueuePublisher messageQueuePublisher;
    private final ProviderMessageJpaRepository repository;

    public MessageProviderService(
            MessageQueuePublisher messageQueuePublisher,
            ProviderMessageJpaRepository repository
    ) {
        this.messageQueuePublisher = messageQueuePublisher;
        this.repository = repository;
    }

    @Override
    public void queueMessage(ProviderMessage message) {
        ProviderMessageEntity entity = new ProviderMessageEntity(
                message.getProviderType(),
                message.getRecipient(),
                message.getContent(),
                message.getSubject(),
                MessageStatus.QUEUED
        );

        ProviderMessageEntity savedEntity = repository.save(entity);

        ProviderMessage messageWithId = ProviderMessageMapper.toDomain(savedEntity);

        messageQueuePublisher.publish(messageWithId);
    }
}