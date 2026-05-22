package com.api.RestAPI.infrastructure.persistence.message.mapper;

import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.persistence.message.entity.ProviderMessageEntity;

public class ProviderMessageMapper {

    public static ProviderMessage toDomain(ProviderMessageEntity entity) {
        return new ProviderMessage(
                entity.getProviderType(),
                entity.getRecipient(),
                entity.getContent(),
                entity.getSubject(),
                entity.getId()
        );
    }
}