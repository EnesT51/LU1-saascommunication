package com.api.RestAPI.application.messageprovider.service;

import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.message.model.ProviderSendResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderDispatcher {

    private final List<MessageProvider> providers;
    private final ProviderMessageStatusService statusService;

    public ProviderDispatcher(
            List<MessageProvider> providers,
            ProviderMessageStatusService statusService
    ) {
        this.providers = providers;
        this.statusService = statusService;
    }

    public void dispatch(ProviderMessage message) {
        MessageProvider provider = providers.stream()
                .filter(p -> p.supports() == message.getProviderType())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No provider found for: " + message.getProviderType()
                ));

        ProviderSendResult result = provider.send(message);

        if (result.isSuccess()) {
            statusService.markAsSent(message.getId(), result.getTrackingId());
        } else {
            statusService.markAsFailed(message.getId(), result.getErrorMessage());
        }
    }
}