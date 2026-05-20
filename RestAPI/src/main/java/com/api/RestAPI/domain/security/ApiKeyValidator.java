package com.api.RestAPI.domain.security;

public interface ApiKeyValidator {

    boolean isValid(String apiKey);
}
