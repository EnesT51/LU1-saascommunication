package com.api.RestAPI.infrastructure.provider.asyncflow.dto;

public class AsyncFlowResponse {

    private boolean accepted;
    private String trackingId;
    private String message;
    private String submittedAt;

    public boolean isAccepted() {
        return accepted;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getMessage() {
        return message;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }
}