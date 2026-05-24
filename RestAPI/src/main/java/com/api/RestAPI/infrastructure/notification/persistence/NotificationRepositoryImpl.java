package com.api.RestAPI.infrastructure.notification.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;
import com.api.RestAPI.infrastructure.notification.entities.Notification;
import com.api.RestAPI.presentation.notification.response.NotificationSummaryResponse;

@Repository
public class NotificationRepositoryImpl implements INotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;

    public NotificationRepositoryImpl(JpaNotificationRepository jpaNotificationRepository) {
        this.jpaNotificationRepository = jpaNotificationRepository;
    }

    @Override
    public void save(Notification notification) {
        jpaNotificationRepository.save(notification);
    }

    @Override
    public void saveAll(List<Notification> notifications) {
        jpaNotificationRepository.saveAll(notifications);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaNotificationRepository.findById(id);
    }

    @Override
    public List<Notification> findPendingNotifications(NotificationStatus status, Instant now) {
        return jpaNotificationRepository.findByStatusAndScheduledAtBefore(status, now);
    }

    @Override
    public List<Notification> findFailedNotifications(NotificationStatus status, int maxRetries) {
        return jpaNotificationRepository.findByStatusAndRetryCountLessThan(status, maxRetries);
    }

    @Override
    public List<Notification> findPendingForAppointment(String appointmentId) {
        return jpaNotificationRepository.findByAppointmentIdAndStatus(
                appointmentId, NotificationStatus.PENDING);
    }

    @Override
    public List<NotificationSummaryResponse> getSummary() {
        List<Object[]> rows = jpaNotificationRepository.findSummaryRaw();

        // Groepeer per organisatie+provider en tel per status
        Map<String, long[]> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String org      = row[0] != null ? (String) row[0] : "unknown";
            String provider = row[1] != null ? (String) row[1] : "unknown";
            NotificationStatus status = (NotificationStatus) row[2];
            long count = ((Number) row[3]).longValue();

            String key = org + "|" + provider;
            map.putIfAbsent(key, new long[3]); // [sent, failed, cancelled]
            long[] counts = map.get(key);

            if (status == NotificationStatus.SENT)      counts[0] += count;
            else if (status == NotificationStatus.FAILED)    counts[1] += count;
            else if (status == NotificationStatus.CANCELLED) counts[2] += count;
        }

        List<NotificationSummaryResponse> result = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : map.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            long[] counts = entry.getValue();
            result.add(new NotificationSummaryResponse(parts[0], parts[1], counts[0], counts[1], counts[2]));
        }
        return result;
    }

    @Override
    public long deleteByCreatedAtBefore(Instant cutoffDate) {
        return jpaNotificationRepository.deleteByCreatedAtBefore(cutoffDate);
    }

    @Override
    public int anonymizeAppointmentIdsBefore(Instant cutoff) {
        return jpaNotificationRepository.anonymizeAppointmentIdsByAppointmentEndBefore(cutoff);
    }
}
