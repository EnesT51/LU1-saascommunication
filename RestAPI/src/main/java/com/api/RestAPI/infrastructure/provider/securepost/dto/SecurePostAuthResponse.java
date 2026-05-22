package com.api.RestAPI.infrastructure.provider.securepost.dto;

public class SecurePostAuthResponse {

    private String accessToken;
    private String tokenType;
    private int expiresIn;
    private String issuedAt;

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public int getExpiresIn() { return expiresIn; }
    public String getIssuedAt() { return issuedAt; }
}