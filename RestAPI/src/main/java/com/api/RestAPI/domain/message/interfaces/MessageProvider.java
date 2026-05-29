package com.api.RestAPI.domain.message.interfaces;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.message.model.ProviderSendResult;
public interface MessageProvider {

    ProviderType supports();

    ProviderSendResult send(ProviderMessage message);
}