package com.api.RestAPI.presentation.messageprovider.controller;

import com.api.RestAPI.application.messageprovider.interfaces.MessageProviderUseCase;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.presentation.messageprovider.request.SendMessageRequest;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/message-provider")
public class MessageProviderController {

    private final MessageProviderUseCase messageProviderUseCase;

    public MessageProviderController(MessageProviderUseCase messageProviderUseCase) {
        this.messageProviderUseCase = messageProviderUseCase;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestBody SendMessageRequest request) {

        ProviderMessage message = new ProviderMessage(
                ProviderType.valueOf(request.getProvider().toUpperCase()),
                request.getRecipient(),
                request.getContent(),
                request.getSubject()
        );

        messageProviderUseCase.queueMessage(message);

        return "Message queued successfully";
    }
}