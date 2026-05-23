package com.api.RestAPI.infrastructure.appointment.persistence;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

import jakarta.transaction.Transactional;

@Repository
public interface JpaAnonymizeAppointmentRepository  extends JpaRepository<AppointmentEntity, Long> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE AppointmentEntity a SET
            a.patientName = null,
            a.patientPhoneNumber = null,
            a.comment = null,
            a.location = null WHERE a.createdAt < :cutoffDate""")
    int anonymizeAppointmentsOlderThan(@Param("cutoffDate")Instant cutoffDate);
}
