package com.api.RestAPI.infrastructure.provider.swiftsend;

import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.provider.swiftsend.dto.SwiftSendRequest;
import com.api.RestAPI.infrastructure.provider.swiftsend.dto.SwiftSendResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Arrays;

@Component
public class SwiftSendProvider implements MessageProvider {

    private static final String URL = "http://host.docker.internal:1337/swiftsend";
    private static final String API_KEY = "your-api-key-here";
    private static final String STUDENT_GROUP = "groep-avans-02";

    private final RestTemplate restTemplate = new RestTemplate();

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

        headers.set("x-api-key", API_KEY);
        headers.set("X-STUDENT-GROUP", STUDENT_GROUP);

        HttpEntity<SwiftSendRequest> entity =
                new HttpEntity<>(request, headers);

        try {

            ResponseEntity<SwiftSendResponse> response =
                    restTemplate.postForEntity(
                            URL,
                            entity,
                            SwiftSendResponse.class
                    );

            SwiftSendResponse body = response.getBody();

            System.out.println("SwiftSend response:");
            System.out.println(String.format("Success: %s", body.isSuccess()));
            System.out.println(String.format("Message ID: %s", body.getMessageId()));

        } catch (Exception ex) {

            System.out.println("SwiftSend error:");
            System.out.println(String.valueOf(ex.getMessage()));
        }
    }
}