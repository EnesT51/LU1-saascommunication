package com.api.RestAPI.application.messageprovider.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import com.api.RestAPI.domain.message.interfaces.DeadLetterMessageQueuePublisher;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.message.model.ProviderSendResult;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

@DisplayName("ProviderDispatcher Tests")
class ProviderDispatcherTest {

    @Test
    @DisplayName("Should send message through matching provider and mark notification SENT")
    void dispatchSendsMessageThroughMatchingProvider() {
        MessageProvider swiftSend = mock(MessageProvider.class);
        MessageProvider securePost = mock(MessageProvider.class);
        INotificationRepository notificationRepository = mock(INotificationRepository.class);
        INotificationMetricsRecorder metricsRecorder = mock(INotificationMetricsRecorder.class);
        OrganizationProviderMapper providerMapper = mock(OrganizationProviderMapper.class);
        DeadLetterMessageQueuePublisher deadLetterPublisher = mock(DeadLetterMessageQueuePublisher.class);
        Notification notification = mock(Notification.class);

        UUID notificationId = UUID.randomUUID();
        ProviderMessage message = new ProviderMessage(
                ProviderType.SECUREPOST, "recipient", "content", "subject", UUID.randomUUID(), notificationId);

        when(swiftSend.supports()).thenReturn(ProviderType.SWIFTSEND);
        when(securePost.supports()).thenReturn(ProviderType.SECUREPOST);
        when(securePost.send(message)).thenReturn(ProviderSendResult.success("tracking-123"));
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        ProviderDispatcher dispatcher = new ProviderDispatcher(
                List.of(swiftSend, securePost), notificationRepository, metricsRecorder, providerMapper, deadLetterPublisher);
        dispatcher.dispatch(message);

        verify(securePost).send(message);
        verify(swiftSend, never()).send(message);
        verify(notification).markAsSent("SECUREPOST");
        verify(notificationRepository).save(notification);
        verify(metricsRecorder).recordSent("SECUREPOST");
    }

    @Test
    @DisplayName("Should retry primary provider 3 times then try fallback providers")
    void dispatchRetriesPrimaryThenFallbacksWhenPrimaryFails() {
        MessageProvider swiftSend = mock(MessageProvider.class);
        MessageProvider securePost = mock(MessageProvider.class);
        INotificationRepository notificationRepository = mock(INotificationRepository.class);
        INotificationMetricsRecorder metricsRecorder = mock(INotificationMetricsRecorder.class);
        OrganizationProviderMapper providerMapper = mock(OrganizationProviderMapper.class);
        DeadLetterMessageQueuePublisher deadLetterPublisher = mock(DeadLetterMessageQueuePublisher.class);
        Notification notification = mock(Notification.class);

        UUID notificationId = UUID.randomUUID();
        ProviderMessage message = new ProviderMessage(
                ProviderType.SWIFTSEND, "recipient", "content", "subject", UUID.randomUUID(), notificationId);

        when(swiftSend.supports()).thenReturn(ProviderType.SWIFTSEND);
        when(securePost.supports()).thenReturn(ProviderType.SECUREPOST);
        when(swiftSend.send(message)).thenReturn(ProviderSendResult.failed("HTTP 503"));
        when(securePost.send(org.mockito.ArgumentMatchers.any())).thenReturn(ProviderSendResult.success("tracking-abc"));
        when(providerMapper.resolveFallbacks(ProviderType.SWIFTSEND)).thenReturn(List.of(ProviderType.SECUREPOST));
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        ProviderDispatcher dispatcher = new ProviderDispatcher(
                List.of(swiftSend, securePost), notificationRepository, metricsRecorder, providerMapper, deadLetterPublisher);
        dispatcher.dispatch(message);

        // Primary geprobeerd 3 keer
        verify(swiftSend, times(3)).send(message);
        // Fallback minstens 1 keer aangeroepen
        verify(securePost, atLeast(1)).send(org.mockito.ArgumentMatchers.any());
        // Uiteindelijk SENT via fallback
        verify(notification).markAsSent("SECUREPOST");
        verify(metricsRecorder).recordSent("SECUREPOST");
    }

    @Test
    @DisplayName("Should publish to DLQ and mark FAILED when all providers fail")
    void dispatchPublishesToDlqWhenAllProvidersFail() {
        MessageProvider swiftSend = mock(MessageProvider.class);
        MessageProvider securePost = mock(MessageProvider.class);
        INotificationRepository notificationRepository = mock(INotificationRepository.class);
        INotificationMetricsRecorder metricsRecorder = mock(INotificationMetricsRecorder.class);
        OrganizationProviderMapper providerMapper = mock(OrganizationProviderMapper.class);
        DeadLetterMessageQueuePublisher deadLetterPublisher = mock(DeadLetterMessageQueuePublisher.class);
        Notification notification = mock(Notification.class);

        UUID notificationId = UUID.randomUUID();
        ProviderMessage message = new ProviderMessage(
                ProviderType.SWIFTSEND, "recipient", "content", "subject", UUID.randomUUID(), notificationId);

        when(swiftSend.supports()).thenReturn(ProviderType.SWIFTSEND);
        when(securePost.supports()).thenReturn(ProviderType.SECUREPOST);
        when(swiftSend.send(org.mockito.ArgumentMatchers.any())).thenReturn(ProviderSendResult.failed("HTTP 503"));
        when(securePost.send(org.mockito.ArgumentMatchers.any())).thenReturn(ProviderSendResult.failed("HTTP 503"));
        when(providerMapper.resolveFallbacks(ProviderType.SWIFTSEND)).thenReturn(List.of(ProviderType.SECUREPOST));
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        ProviderDispatcher dispatcher = new ProviderDispatcher(
                List.of(swiftSend, securePost), notificationRepository, metricsRecorder, providerMapper, deadLetterPublisher);
        dispatcher.dispatch(message);

        // Primary 3x geprobeerd, fallback 3x geprobeerd
        verify(swiftSend, times(3)).send(org.mockito.ArgumentMatchers.any());
        verify(securePost, times(3)).send(org.mockito.ArgumentMatchers.any());
        // Naar DLQ
        verify(deadLetterPublisher).publishToDlq(org.mockito.ArgumentMatchers.any());
        // FAILED status
        verify(notification).markAsFailed("Alle providers gefaald");
        verify(metricsRecorder).recordFailed("SWIFTSEND", "alle_providers_mislukt");
    }

    @Test
    @DisplayName("Should throw when no provider supports message")
    void dispatchThrowsWhenNoProviderSupportsMessage() {
        MessageProvider swiftSend = mock(MessageProvider.class);
        INotificationRepository notificationRepository = mock(INotificationRepository.class);
        INotificationMetricsRecorder metricsRecorder = mock(INotificationMetricsRecorder.class);
        OrganizationProviderMapper providerMapper = mock(OrganizationProviderMapper.class);
        DeadLetterMessageQueuePublisher deadLetterPublisher = mock(DeadLetterMessageQueuePublisher.class);

        ProviderMessage message = new ProviderMessage(
                ProviderType.ASYNCFLOW, "recipient", "content", "subject", UUID.randomUUID());
        when(swiftSend.supports()).thenReturn(ProviderType.SWIFTSEND);

        ProviderDispatcher dispatcher = new ProviderDispatcher(
                List.of(swiftSend), notificationRepository, metricsRecorder, providerMapper, deadLetterPublisher);

        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(message));
    }
}
