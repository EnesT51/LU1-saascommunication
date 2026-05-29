package com.api.RestAPI.application.security.interfaces;

public interface IApiKeyValidationService {
    boolean isValidApiKey(String apiKey);

}
