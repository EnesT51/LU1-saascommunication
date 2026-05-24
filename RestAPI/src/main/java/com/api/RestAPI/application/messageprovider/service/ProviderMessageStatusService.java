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
        repository.findById(messageId).ifPresent(entity -> {
            entity.markAsSent(trackingId);
            repository.save(entity);
        });
    }

    public void markAsFailed(UUID messageId, String errorMessage) {
        repository.findById(messageId).ifPresent(entity -> {
            entity.markAsFailed(errorMessage);
            repository.save(entity);
        });
    }

    public void markAsDeadLetter(UUID messageId, String errorMessage) {
        repository.findById(messageId).ifPresent(entity -> {
            entity.markAsDeadLetter(errorMessage);
            repository.save(entity);
        });
    }

    public void markAsRetrying(UUID messageId, String errorMessage) {
        repository.findById(messageId).ifPresent(entity -> {
            entity.markAsRetrying(errorMessage);
            repository.save(entity);
        });
    }
}