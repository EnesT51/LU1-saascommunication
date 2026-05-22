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

import java.util.Arrays;

@Component
public class SwiftSendProvider implements MessageProvider {

    private static final Logger logger = LoggerFactory.getLogger(SwiftSendProvider.class);

    private final RestTemplate restTemplate;
    private final SwiftSendProperties properties;

    public SwiftSendProvider(SwiftSendProperties properties) {
        this.properties = properties;
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
                logger.info("SwiftSend message sent successfully. MessageId={}", body.getMessageId());
            } else {
                logger.warn("SwiftSend failed. Error={}", body != null ? body.getError() : "No response body");
            }

        } catch (Exception ex) {
            logger.error("SwiftSend request failed: {}", ex.getMessage());
            throw ex;
        }
    }
}