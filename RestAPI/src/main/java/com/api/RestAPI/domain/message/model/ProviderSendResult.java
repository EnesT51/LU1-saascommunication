package com.api.RestAPI.domain.message.model;

public class ProviderSendResult {

    private final boolean success;
    private final String trackingId;
    private final String errorMessage;

    private ProviderSendResult(boolean success, String trackingId, String errorMessage) {
        this.success = success;
        this.trackingId = trackingId;
        this.errorMessage = errorMessage;
    }

    public static ProviderSendResult success(String trackingId) {
        return new ProviderSendResult(true, trackingId, null);
    }

    public static ProviderSendResult failed(String errorMessage) {
        return new ProviderSendResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}