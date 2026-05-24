package com.api.RestAPI.domain.message.interfaces;

import com.api.RestAPI.domain.message.model.ProviderMessage;

public interface DeadLetterMessageQueuePublisher {
    void publishToDlq(ProviderMessage message);
}
