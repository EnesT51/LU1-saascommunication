package com.api.RestAPI.application.notification.services;

import org.springframework.stereotype.Service;

import com.api.RestAPI.application.notification.interfaces.INotificationMetricsRecorder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class NotificationMetricsRecorder implements INotificationMetricsRecorder {

    private final MeterRegistry meterRegistry;

    public NotificationMetricsRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordSent(String provider) {
        Counter.builder("notifications_sent_total")
                .description("Aantal succesvol verstuurde notificaties")
                .tag("provider", provider)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordFailed(String provider, String reason) {
        Counter.builder("notifications_failed_total")
                .description("Aantal mislukte notificaties")
                .tag("provider", provider)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }
}
