package com.api.RestAPI.infrastructure.notification.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import com.api.RestAPI.infrastructure.notification.entities.Notification;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;

@Repository
public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByStatusAndScheduledAtBefore(NotificationStatus status, Instant now);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);

    List<Notification> findByAppointmentIdAndStatus(String appointmentId, NotificationStatus status);

    long deleteByCreatedAtBefore(Instant cutoffDate);

    @Query("SELECT n.organizationId, n.provider, n.status, COUNT(n) " +
           "FROM Notification n " +
           "GROUP BY n.organizationId, n.provider, n.status " +
           "ORDER BY n.organizationId, n.provider")
    List<Object[]> findSummaryRaw();

    /**
     * NFR 11: Verwijder direct identificeerbare afspraakgegevens uit de notificatie-meta-informatie.
     * Zet appointmentId op 'ANONYMIZED' voor notificaties waarvan de gekoppelde afspraak
     * vóór de cutoffdatum is afgelopen. Bewaart alleen: organizationId, provider, status (voor facturatie).
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.appointmentId = 'ANONYMIZED' " +
           "WHERE n.appointmentId != 'ANONYMIZED' " +
           "AND n.appointmentId IN " +
           "(SELECT a.appointmentId FROM AppointmentEntity a WHERE a.end < :cutoff)")
    int anonymizeAppointmentIdsByAppointmentEndBefore(@Param("cutoff") Instant cutoff);
}
