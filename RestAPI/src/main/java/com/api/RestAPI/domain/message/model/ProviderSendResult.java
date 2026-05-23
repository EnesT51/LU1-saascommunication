package com.api.RestAPI.domain.message.model;

public class ProviderSendResult {

    private final boolean success;
    private final String trackingId;
    private final String errorMessage;
    private final boolean retryable;

    private ProviderSendResult(
            boolean success,
            String trackingId,
            String errorMessage,
            boolean retryable
    ) {
        this.success = success;
        this.trackingId = trackingId;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
    }

    public static ProviderSendResult success(String trackingId) {
        return new ProviderSendResult(true, trackingId, null, false);
    }

    public static ProviderSendResult failed(String errorMessage) {
        return new ProviderSendResult(false, null, errorMessage, false);
    }

    public static ProviderSendResult retryableFailure(String errorMessage) {
        return new ProviderSendResult(false, null, errorMessage, true);
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

    public boolean isRetryable() {
        return retryable;
    }
}