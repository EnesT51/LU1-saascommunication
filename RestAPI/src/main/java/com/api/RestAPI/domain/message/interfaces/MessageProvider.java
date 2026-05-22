package com.api.RestAPI.domain.message.interfaces;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.model.ProviderMessage;
public interface MessageProvider {

    ProviderType supports();

    void send(ProviderMessage message);
}