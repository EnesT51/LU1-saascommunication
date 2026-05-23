package com.api.RestAPI.presentation.messageprovider.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.application.messageprovider.interfaces.MessageProviderUseCase;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.presentation.messageprovider.request.SendMessageRequest;

@DisplayName("MessageProviderController Tests")
class MessageProviderControllerTest {

    @Test
    @DisplayName("Should queue message from request")
    void sendMessageQueuesProviderMessage() {
        MessageProviderUseCase useCase = mock(MessageProviderUseCase.class);
        MessageProviderController controller = new MessageProviderController(useCase);
        SendMessageRequest request = new SendMessageRequest();
        request.setProvider("swiftsend");
        request.setRecipient("recipient");
        request.setContent("content");
        request.setSubject("subject");

        String response = controller.sendMessage(request);

        assertEquals("Message queued successfully", response);
        verify(useCase).queueMessage(argThat(message ->
                message.getProviderType() == ProviderType.SWIFTSEND
                        && "recipient".equals(message.getRecipient())
                        && "content".equals(message.getContent())
                        && "subject".equals(message.getSubject())
        ));
    }

    @Test
    @DisplayName("Should reject unknown provider")
    void sendMessageRejectsUnknownProvider() {
        MessageProviderUseCase useCase = mock(MessageProviderUseCase.class);
        MessageProviderController controller = new MessageProviderController(useCase);
        SendMessageRequest request = new SendMessageRequest();
        request.setProvider("unknown");

        assertThrows(IllegalArgumentException.class, () -> controller.sendMessage(request));
    }
}
