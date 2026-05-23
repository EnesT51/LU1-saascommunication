package com.api.RestAPI.infrastructure.appointment.persistence;

import com.api.RestAPI.domain.appointment.Interface.AnonymizeAppointmentRepository;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class AppointmentRepositoryImpl implements IAppointmentRepository, AnonymizeAppointmentRepository {

    private final JpaAppointmentRepository jpaAppointmentRepository;
    private final JpaAnonymizeAppointmentRepository jpaAnonymizeAppointmentRepository;

    public AppointmentRepositoryImpl(JpaAppointmentRepository jpaAppointmentRepository, JpaAnonymizeAppointmentRepository jpaAnonymizeAppointmentRepository) {
        this.jpaAppointmentRepository = jpaAppointmentRepository;
        this.jpaAnonymizeAppointmentRepository = jpaAnonymizeAppointmentRepository;
    }

    public List<AppointmentEntity> getAppointments() {
        return jpaAppointmentRepository.findAll();
    }

    public Optional<AppointmentEntity> findById(UUID id) {
        return jpaAppointmentRepository.findById(id);
    }

    @Override
    public Optional<AppointmentEntity> findByAppointmentId(String appointmentId) {
        return jpaAppointmentRepository.findByAppointmentId(appointmentId);
    }

    @Override
    public AppointmentEntity save(AppointmentEntity appointment) {
        return jpaAppointmentRepository.save(appointment);
    }

    @Override
    public long deleteByCreatedAtBefore(Instant cutoffDate) {
        return jpaAppointmentRepository.deleteByCreatedAtBefore(cutoffDate);
    }
    @Override
    public int anonymizeAppointmentsEndedBefore(Instant cutoffDate) {
        return jpaAnonymizeAppointmentRepository.anonymizeAppointmentsEndedBefore(cutoffDate);
    }
}

