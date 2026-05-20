package com.api.RestAPI.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.api.RestAPI.domain.security.ApiKeyValidator;

@Component
public class EnvironmentApiKeyValidator implements ApiKeyValidator {

    private final String validApiKey;

    public EnvironmentApiKeyValidator(@Value("${security.api-key}") String validApiKey) {this.validApiKey = validApiKey;}

    @Override
    public boolean isValid(String apiKey) {

        return constantTimeEquals(apiKey, validApiKey);
    }
    private boolean constantTimeEquals(String a, String b) {

        if (a == null || b == null) {
            return false;
        }

        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;

        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }
    
}
