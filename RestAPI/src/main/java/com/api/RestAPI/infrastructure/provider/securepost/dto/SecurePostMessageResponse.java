package com.api.RestAPI.infrastructure.provider.securepost.dto;

public class SecurePostMessageResponse {

    private boolean delivered;
    private String trackingId;
    private String errorMessage;
    private String deliveryTimestamp;

    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }
    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getDeliveryTimestamp() { return deliveryTimestamp; }
    public void setDeliveryTimestamp(String deliveryTimestamp) { this.deliveryTimestamp = deliveryTimestamp; }
}