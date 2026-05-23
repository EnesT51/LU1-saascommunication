package com.api.RestAPI.presentation.messageprovider.controller;

import com.api.RestAPI.application.messageprovider.interfaces.MessageProviderUseCase;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.presentation.messageprovider.request.SendMessageRequest;

import org.springframework.web.bind.annotation.*;

import com.api.RestAPI.infrastructure.persistence.message.repository.ProviderMessageJpaRepository;
import com.api.RestAPI.presentation.messageprovider.response.ProviderMessageResponse;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/message-provider")
public class MessageProviderController {

    private final MessageProviderUseCase messageProviderUseCase;
    private final ProviderMessageJpaRepository repository;

    public MessageProviderController(
        MessageProviderUseCase messageProviderUseCase,
        ProviderMessageJpaRepository repository
    ) {
        this.messageProviderUseCase = messageProviderUseCase;
        this.repository = repository;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestBody SendMessageRequest request) {

        ProviderMessage message = new ProviderMessage(
        ProviderType.valueOf(request.getProvider().toUpperCase()),
        request.getRecipient(),
        request.getContent(),
        request.getSubject(),
        null
);

        messageProviderUseCase.queueMessage(message);

        return "Message queued successfully";
    }
    @GetMapping
    public List<ProviderMessageResponse> getMessages() {
        return repository.findAll()
                .stream()
                .map(message -> new ProviderMessageResponse(
                        message.getId(),
                        message.getProviderType(),
                        message.getRecipient(),
                        message.getStatus(),
                        message.getTrackingId(),
                        message.getErrorMessage(),
                        message.getCreatedAt(),
                        message.getSentAt()
                ))
                    .collect(Collectors.toList());
    }
    @GetMapping("/{id}")
    public ProviderMessageResponse getMessageById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(message -> new ProviderMessageResponse(
                        message.getId(),
                        message.getProviderType(),
                        message.getRecipient(),
                        message.getStatus(),
                        message.getTrackingId(),
                        message.getErrorMessage(),
                        message.getCreatedAt(),
                        message.getSentAt()
                ))
                .orElseThrow(() -> new IllegalArgumentException(String.format("Message not found: %s", id)));
    }
}
