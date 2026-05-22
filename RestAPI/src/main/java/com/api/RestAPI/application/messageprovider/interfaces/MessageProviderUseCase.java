package com.api.RestAPI.application.messageprovider.interfaces;

import com.api.RestAPI.domain.message.model.ProviderMessage;

public interface MessageProviderUseCase {
    void queueMessage(ProviderMessage message);
}