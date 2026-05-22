package com.api.RestAPI.infrastructure.provider.asyncflow;

import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.provider.asyncflow.dto.AsyncFlowRequest;
import com.api.RestAPI.infrastructure.provider.asyncflow.dto.AsyncFlowResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AsyncFlowProvider implements MessageProvider {

    private static final Logger logger = LoggerFactory.getLogger(AsyncFlowProvider.class);

    private final RestTemplate restTemplate;
    private final AsyncFlowProperties properties;

    public AsyncFlowProvider(AsyncFlowProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ProviderType supports() {
        return ProviderType.ASYNCFLOW;
    }

    @Override
    public void send(ProviderMessage message) {
        AsyncFlowRequest request = new AsyncFlowRequest(
                message.getRecipient(),
                message.getContent(),
                properties.getPriority()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", properties.getApiKey());
        headers.set("X-STUDENT-GROUP", properties.getStudentGroup());

        HttpEntity<AsyncFlowRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<AsyncFlowResponse> response = restTemplate.postForEntity(
                    properties.getUrl(),
                    entity,
                    AsyncFlowResponse.class
            );

            AsyncFlowResponse body = response.getBody();

            if (body != null && body.isAccepted()) {
                logger.info("AsyncFlow message accepted. TrackingId={}", body.getTrackingId());
            } else {
                logger.warn("AsyncFlow failed. Message={}", body != null ? body.getMessage() : "No response body");
            }

        } catch (Exception ex) {
            logger.error("AsyncFlow request failed: {}", ex.getMessage());
            throw ex;
        }
    }
}