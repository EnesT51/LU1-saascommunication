package com.api.RestAPI.infrastructure.provider.legacylink;

import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.message.model.ProviderSendResult;
import com.api.RestAPI.infrastructure.provider.legacylink.dto.LegacyLinkResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LegacyLinkProvider implements MessageProvider {

    private static final Logger logger = LoggerFactory.getLogger(LegacyLinkProvider.class);

    private final RestTemplate restTemplate;
    private final LegacyLinkProperties properties;

    public LegacyLinkProvider(LegacyLinkProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ProviderType supports() {
        return ProviderType.LEGACYLINK;
    }

    @Override
    public ProviderSendResult send(ProviderMessage message) {
        String xmlBody = String.format(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                        "<SendSmsRequest xmlns=\"http://legacylink.fakecomworld.com/v1\">" +
                        "<PhoneNumber>%s</PhoneNumber>" +
                        "<MessageText>%s</MessageText>" +
                        "<SenderIdentification>%s</SenderIdentification>" +
                        "</SendSmsRequest>",
                message.getRecipient(),
                message.getContent(),
                properties.getSender()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_XML));
        headers.set("Authorization", properties.getAuthorization());
        headers.set("X-STUDENT-GROUP", properties.getStudentGroup());

        HttpEntity<String> entity = new HttpEntity<>(xmlBody, headers);

        try {
            ResponseEntity<LegacyLinkResponse> response = restTemplate.postForEntity(
                    properties.getUrl(),
                    entity,
                    LegacyLinkResponse.class
            );

            LegacyLinkResponse body = response.getBody();

            if (body != null && body.getStatusCode() == 200) {
                logger.info("LegacyLink SMS sent successfully. MessageReference={}", body.getMessageReference());
                return ProviderSendResult.success(body.getMessageReference());
            }

            String error = body != null ? body.getStatusMessage() : "No response body";
            logger.warn("LegacyLink failed. StatusMessage={}", error);
            return ProviderSendResult.failed(error);

        } catch (Exception ex) {

            String errorMessage = ex.getMessage();

            logger.error("LegacyLink request failed: {}", errorMessage);

            if (
                    errorMessage != null &&
                    (
                            errorMessage.contains("401") ||
                            errorMessage.contains("403")
                    )
            ) {
                return ProviderSendResult.failed(errorMessage);
            }

            return ProviderSendResult.retryableFailure(errorMessage);
        }
    }
}