package com.api.RestAPI.application.notification.interfaces;

public interface INotificationMetricsRecorder {
    void recordSent(String provider);
    void recordFailed(String provider, String reason);
}
