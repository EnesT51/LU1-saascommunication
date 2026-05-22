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
        ProviderMessageEntity entity = repository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException(new StringBuilder("Message not found: ").append(messageId).toString()));

        entity.markAsSent(trackingId);
        repository.save(entity);
    }

    public void markAsFailed(UUID messageId, String errorMessage) {
        ProviderMessageEntity entity = repository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException(new StringBuilder("Message not found: ").append(messageId).toString()));

        entity.markAsFailed(errorMessage);
        repository.save(entity);
    }
}