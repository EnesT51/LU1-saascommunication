package com.api.RestAPI.infrastructure.provider.securepost.dto;

public class SecurePostMessageResponse {

    private boolean delivered;
    private String trackingId;
    private String errorMessage;
    private String deliveryTimestamp;

    public boolean isDelivered() { return delivered; }
    public String getTrackingId() { return trackingId; }
    public String getErrorMessage() { return errorMessage; }
    public String getDeliveryTimestamp() { return deliveryTimestamp; }
}