package com.api.RestAPI.application.messageprovider.service;

import com.api.RestAPI.infrastructure.persistence.message.entity.ProviderMessageEntity;
import com.api.RestAPI.infrastructure.persistence.message.repository.ProviderMessageJpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProviderMessageStatusService {

    private final ProviderMessageJpaRepository repository;

    public ProviderMessageStatusService(ProviderMessageJpaRepository repository) {
        this.repository = repository;
    }

    public void markAsSent(UUID messageId, String trackingId) {
        java.util.Optional<ProviderMessageEntity> opt = repository.findById(messageId);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException(new StringBuilder("Message not found: ").append(messageId).toString());
        }
        ProviderMessageEntity entity = opt.get();
        entity.markAsSent(trackingId);
        repository.save(entity);
    }

    public void markAsFailed(UUID messageId, String errorMessage) {
        java.util.Optional<ProviderMessageEntity> opt = repository.findById(messageId);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException(new StringBuilder("Message not found: ").append(messageId).toString());
        }
        ProviderMessageEntity entity = opt.get();
        entity.markAsFailed(errorMessage);
        repository.save(entity);
    }
    public void markAsDeadLetter(UUID messageId, String errorMessage) {
        java.util.Optional<ProviderMessageEntity> opt = repository.findById(messageId);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException(new StringBuilder("Message not found: ").append(messageId).toString());
        }
        ProviderMessageEntity entity = opt.get();
        entity.markAsDeadLetter(errorMessage);
        repository.save(entity);
    }
    public void markAsRetrying(UUID messageId, String errorMessage) {
        java.util.Optional<ProviderMessageEntity> opt = repository.findById(messageId);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException(new StringBuilder("Message not found: ").append(messageId).toString());
        }
        ProviderMessageEntity entity = opt.get();
        entity.markAsRetrying(errorMessage);
        repository.save(entity);
    }
}