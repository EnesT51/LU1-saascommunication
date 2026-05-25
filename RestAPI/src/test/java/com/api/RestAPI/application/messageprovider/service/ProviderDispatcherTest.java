package com.api.RestAPI.application.messageprovider.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.application.notification.interfaces.INotificationMetricsRecorder;
import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.message.model.ProviderSendResult;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

@DisplayName("ProviderDispatcher Tests")
class ProviderDispatcherTest {

    @Test
    @DisplayName("Should send message through matching provider and mark notification SENT")
    void dispatchSendsMessageThroughMatchingProvider() {
        // Arrange
        MessageProvider swiftSend = mock(MessageProvider.class);
        MessageProvider securePost = mock(MessageProvider.class);
        INotificationRepository notificationRepository = mock(INotificationRepository.class);
        INotificationMetricsRecorder metricsRecorder = mock(INotificationMetricsRecorder.class);
        Notification notification = mock(Notification.class);

        UUID notificationId = UUID.randomUUID();
        ProviderMessage message = new ProviderMessage(
                ProviderType.SECUREPOST, "recipient", "content", "subject", UUID.randomUUID(), notificationId);

        when(swiftSend.supports()).thenReturn(ProviderType.SWIFTSEND);
        when(securePost.supports()).thenReturn(ProviderType.SECUREPOST);
        when(securePost.send(message)).thenReturn(ProviderSendResult.success("tracking-123"));
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        ProviderDispatcher dispatcher = new ProviderDispatcher(List.of(swiftSend, securePost), notificationRepository, metricsRecorder);
        dispatcher.dispatch(message);

        // Assert
        verify(securePost).send(message);
        verify(swiftSend, never()).send(message);
        verify(notification).markAsSent("SECUREPOST");
        verify(notificationRepository).save(notification);
        verify(metricsRecorder).recordSent("SECUREPOST");
    }

    @Test
    @DisplayName("Should mark notification FAILED when provider fails")
    void dispatchMarksNotificationFailedOnProviderFailure() {
        // Arrange
        MessageProvider swiftSend = mock(MessageProvider.class);
        INotificationRepository notificationRepository = mock(INotificationRepository.class);
        INotificationMetricsRecorder metricsRecorder = mock(INotificationMetricsRecorder.class);
        Notification notification = mock(Notification.class);

        UUID notificationId = UUID.randomUUID();
        ProviderMessage message = new ProviderMessage(
                ProviderType.SWIFTSEND, "recipient", "content", "subject", UUID.randomUUID(), notificationId);

        when(swiftSend.supports()).thenReturn(ProviderType.SWIFTSEND);
        when(swiftSend.send(message)).thenReturn(ProviderSendResult.failed("HTTP 503"));
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        ProviderDispatcher dispatcher = new ProviderDispatcher(List.of(swiftSend), notificationRepository, metricsRecorder);
        dispatcher.dispatch(message);

        // Assert
        verify(notification).markAsFailed("HTTP 503");
        verify(notificationRepository).save(notification);
        verify(metricsRecorder).recordFailed("SWIFTSEND", "http_provider_mislukt");
    }

    @Test
    @DisplayName("Should throw when no provider supports message")
    void dispatchThrowsWhenNoProviderSupportsMessage() {
        // Arrange
        MessageProvider swiftSend = mock(MessageProvider.class);
        INotificationRepository notificationRepository = mock(INotificationRepository.class);
        INotificationMetricsRecorder metricsRecorder = mock(INotificationMetricsRecorder.class);

        ProviderMessage message = new ProviderMessage(
                ProviderType.ASYNCFLOW, "recipient", "content", "subject", UUID.randomUUID());
        when(swiftSend.supports()).thenReturn(ProviderType.SWIFTSEND);

        ProviderDispatcher dispatcher = new ProviderDispatcher(List.of(swiftSend), notificationRepository, metricsRecorder);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(message));
    }
}
