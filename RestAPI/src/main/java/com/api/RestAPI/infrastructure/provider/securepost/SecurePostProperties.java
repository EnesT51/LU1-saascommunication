package com.api.RestAPI.infrastructure.provider.securepost;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "providers.securepost")
public class SecurePostProperties {

    private String authUrl;
    private String messageUrl;
    private String clientId;
    private String clientSecret;
    private String studentGroup;

    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

    public String getMessageUrl() { return messageUrl; }
    public void setMessageUrl(String messageUrl) { this.messageUrl = messageUrl; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getStudentGroup() { return studentGroup; }
    public void setStudentGroup(String studentGroup) { this.studentGroup = studentGroup; }
}