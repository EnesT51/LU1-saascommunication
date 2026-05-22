package com.api.RestAPI.application.messageprovider.service;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class ProviderDispatcher {

    private final List<MessageProvider> providers;

    public ProviderDispatcher(List<MessageProvider> providers) {
        this.providers = providers;
    }

    public void dispatch(ProviderMessage message) {
        MessageProvider provider = providers.stream()
                .filter(p -> p.supports() == message.getProviderType())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("No provider found for: %s", message.getProviderType())
                ));

        provider.send(message);
    }
}
