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

import com.api.RestAPI.application.messageprovider.service.ProviderMessageStatusService;

@Component
public class AsyncFlowProvider implements MessageProvider {

    private static final Logger logger = LoggerFactory.getLogger(AsyncFlowProvider.class);

    private final RestTemplate restTemplate;
    private final AsyncFlowProperties properties;
    private final ProviderMessageStatusService statusService;

    public AsyncFlowProvider(AsyncFlowProperties properties, ProviderMessageStatusService statusService) {
        this.properties = properties;
        this.statusService = statusService;
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
                statusService.markAsSent(message.getId(), body.getTrackingId());

                logger.info(
                        "AsyncFlow message accepted. TrackingId={}",
                        body.getTrackingId()
                );
            } else {
                String error = body != null ? body.getMessage() : "No response body";

                statusService.markAsFailed(message.getId(), error);

                logger.warn("AsyncFlow failed. Message={}", error);
            }

        } catch (Exception ex) {
            statusService.markAsFailed(message.getId(), ex.getMessage());
            logger.error("AsyncFlow request failed: {}", ex.getMessage());
            throw ex;
        }
    }
}