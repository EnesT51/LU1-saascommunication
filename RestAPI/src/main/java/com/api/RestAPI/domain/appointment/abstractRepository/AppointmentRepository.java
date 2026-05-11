package com.api.RestAPI.domain.appointment.abstractRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.RestAPI.domain.appointment.entities.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, AppointmentRepositoryCustom {}
