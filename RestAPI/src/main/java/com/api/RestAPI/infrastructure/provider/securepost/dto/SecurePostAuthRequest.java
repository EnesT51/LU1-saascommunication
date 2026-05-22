package com.api.RestAPI.infrastructure.provider.securepost.dto;

public class SecurePostAuthRequest {

    private String clientId;
    private String clientSecret;

    public SecurePostAuthRequest(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
}