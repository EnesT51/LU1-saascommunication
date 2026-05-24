package com.api.RestAPI.application.messageprovider.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.message.model.ProviderSendResult;

@DisplayName("ProviderDispatcher Tests")
class ProviderDispatcherTest {

    @Test
    @DisplayName("Should send message through matching provider")
    void dispatchSendsMessageThroughMatchingProvider() {
        MessageProvider swiftSend = mock(MessageProvider.class);
        MessageProvider securePost = mock(MessageProvider.class);
        ProviderMessage message = new ProviderMessage(ProviderType.SECUREPOST, "recipient", "content", "subject");
        when(swiftSend.supports()).thenReturn(ProviderType.SWIFTSEND);
        when(securePost.supports()).thenReturn(ProviderType.SECUREPOST);
        when(securePost.send(message)).thenReturn(ProviderSendResult.success("tracking-123"));
        ProviderMessageStatusService statusService = mock(ProviderMessageStatusService.class);

        ProviderDispatcher dispatcher = new ProviderDispatcher(List.of(swiftSend, securePost), statusService);
        dispatcher.dispatch(message);

        verify(securePost).send(message);
        verify(swiftSend, never()).send(message);
        verify(statusService).markAsSent(message.getId(), "tracking-123");
    }

    @Test
    @DisplayName("Should throw when no provider supports message")
    void dispatchThrowsWhenNoProviderSupportsMessage() {
        MessageProvider swiftSend = mock(MessageProvider.class);
        ProviderMessage message = new ProviderMessage(ProviderType.ASYNCFLOW, "recipient", "content", "subject");
        when(swiftSend.supports()).thenReturn(ProviderType.SWIFTSEND);
        ProviderMessageStatusService statusService = mock(ProviderMessageStatusService.class);

        ProviderDispatcher dispatcher = new ProviderDispatcher(List.of(swiftSend), statusService);

        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(message));
    }
}
