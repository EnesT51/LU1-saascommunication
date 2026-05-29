package com.api.RestAPI.application.security;

import org.springframework.stereotype.Service;
import com.api.RestAPI.application.security.interfaces.IApiKeyValidationService;
import com.api.RestAPI.domain.security.ApiKeyValidator;

@Service
public class ApiKeyValidationService implements IApiKeyValidationService {
    
    private final ApiKeyValidator apiKeyValidator;

    public ApiKeyValidationService(ApiKeyValidator apiKeyValidator) {
        this.apiKeyValidator = apiKeyValidator;
    }
    @Override
    public boolean isValidApiKey(String apiKey) {

        if(apiKey == null || apiKey.isEmpty()) {
            return false;
        }
        return apiKeyValidator.isValid(apiKey);
    }
}
