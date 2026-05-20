package com.api.RestAPI.infrastructure.appointment.persistence;

import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class AppointmentRepositoryImpl implements IAppointmentRepository {

    private final JpaAppointmentRepository jpaAppointmentRepository;

    public AppointmentRepositoryImpl(JpaAppointmentRepository jpaAppointmentRepository) {
        this.jpaAppointmentRepository = jpaAppointmentRepository;
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
}

