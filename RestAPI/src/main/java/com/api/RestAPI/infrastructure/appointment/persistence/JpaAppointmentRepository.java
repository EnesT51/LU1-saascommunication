package com.api.RestAPI.infrastructure.appointment.persistence;
import java.util.UUID;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

@Repository
public interface JpaAppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    Optional<AppointmentEntity> findByAppointmentId(String appointmentId);
    long deleteByCreatedAtBefore(Instant cutoffDate);
}
