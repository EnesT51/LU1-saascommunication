package com.api.RestAPI.infrastructure.provider.swiftsend;

import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.provider.swiftsend.dto.SwiftSendRequest;
import com.api.RestAPI.infrastructure.provider.swiftsend.dto.SwiftSendResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.api.RestAPI.application.messageprovider.service.ProviderMessageStatusService;

import java.util.Arrays;

@Component
public class SwiftSendProvider implements MessageProvider {

    private static final Logger logger = LoggerFactory.getLogger(SwiftSendProvider.class);

    private final RestTemplate restTemplate;
    private final SwiftSendProperties properties;
    private final ProviderMessageStatusService statusService;
    
    public SwiftSendProvider(
        SwiftSendProperties properties,
        ProviderMessageStatusService statusService
) {
    this.properties = properties;
    this.statusService = statusService;
    this.restTemplate = new RestTemplate();
}

    @Override
    public ProviderType supports() {
        return ProviderType.SWIFTSEND;
    }

    @Override
    public void send(ProviderMessage message) {
        SwiftSendRequest request = new SwiftSendRequest(
                "SMS",
                Arrays.asList(message.getRecipient()),
                message.getContent()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", properties.getApiKey());
        headers.set("X-STUDENT-GROUP", properties.getStudentGroup());

        HttpEntity<SwiftSendRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<SwiftSendResponse> response = restTemplate.postForEntity(
                    properties.getUrl(),
                    entity,
                    SwiftSendResponse.class
            );

            SwiftSendResponse body = response.getBody();

            if (body != null && body.isSuccess()) {
                statusService.markAsSent(message.getId(), body.getMessageId());
                logger.info("SwiftSend message sent successfully. MessageId={}", body.getMessageId());
            } else {
                String error = body != null ? body.getError() : "No response body";
                statusService.markAsFailed(message.getId(), error);
                logger.warn("SwiftSend failed. Error={}", error);
            }

        } catch (Exception ex) {
            statusService.markAsFailed(message.getId(), ex.getMessage());
            logger.error("SwiftSend request failed: {}", ex.getMessage());
        }
    }
}