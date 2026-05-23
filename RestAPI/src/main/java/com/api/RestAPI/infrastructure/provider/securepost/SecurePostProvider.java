package com.api.RestAPI.infrastructure.provider.securepost;

import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.message.model.ProviderSendResult;
import com.api.RestAPI.infrastructure.provider.securepost.dto.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SecurePostProvider implements MessageProvider {

    private static final Logger logger = LoggerFactory.getLogger(SecurePostProvider.class);

    private final RestTemplate restTemplate;
    private final SecurePostProperties properties;

    public SecurePostProvider(SecurePostProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ProviderType supports() {
        return ProviderType.SECUREPOST;
    }

    @Override
    public ProviderSendResult send(ProviderMessage message) {
        try {
            String accessToken = authenticate();

            SecurePostMessageRequest request = new SecurePostMessageRequest(
                    "EMAIL",
                    message.getRecipient(),
                    message.getContent(),
                    message.getSubject() != null ? message.getSubject() : "Afspraak notificatie"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.set("X-STUDENT-GROUP", properties.getStudentGroup());

            HttpEntity<SecurePostMessageRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<SecurePostMessageResponse> response = restTemplate.postForEntity(
                    properties.getMessageUrl(),
                    entity,
                    SecurePostMessageResponse.class
            );

            SecurePostMessageResponse body = response.getBody();

            if (body != null && body.isDelivered()) {
                logger.info("SecurePost message delivered. TrackingId={}", body.getTrackingId());
                return ProviderSendResult.success(body.getTrackingId());
            }

            String error = body != null ? body.getErrorMessage() : "No response body";
            logger.warn("SecurePost failed. Error={}", error);
            return ProviderSendResult.failed(error);

        } catch (Exception ex) {
            String errorMessage = ex.getMessage();

            logger.error("SecurePost message request failed: {}", errorMessage);

            if (
                    errorMessage != null &&
                    (
                            errorMessage.contains("401") ||
                            errorMessage.contains("403") ||
                            errorMessage.contains("400")
                    )
            ) {
                return ProviderSendResult.failed(errorMessage);
            }

            return ProviderSendResult.retryableFailure(errorMessage);
        }
    }

    private String authenticate() {
        SecurePostAuthRequest request = new SecurePostAuthRequest(
                properties.getClientId(),
                properties.getClientSecret()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-STUDENT-GROUP", properties.getStudentGroup());

        HttpEntity<SecurePostAuthRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<SecurePostAuthResponse> response = restTemplate.postForEntity(
                    properties.getAuthUrl(),
                    entity,
                    SecurePostAuthResponse.class
            );

            SecurePostAuthResponse body = response.getBody();

            if (body == null || body.getAccessToken() == null) {
                throw new IllegalStateException("SecurePost authentication failed: no access token returned");
            }

            return body.getAccessToken();

        } catch (Exception ex) {
            logger.error("SecurePost authentication failed: {}", ex.getMessage());
            throw ex;
        }
    }
}