package com.api.RestAPI.application.appointment.service;

import org.springframework.stereotype.Service;

import java.util.List;

import com.api.RestAPI.application.HapiFhir.interfaces.IFhirParser;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import com.api.RestAPI.infrastructure.appointment.persistence.JpaAppointmentRepository;
import org.hl7.fhir.r4.model.Appointment;

@Service
public class AppointmentService implements IAppointmentEventProcessor {

    private final IAppointmentRepository appointmentRepository;
    private final JpaAppointmentRepository jpaAppointmentRepository;
    private final IAppointmentMapper appointmentMapper;
    private final IFhirParser fhirParser;

    public AppointmentService(
        IAppointmentRepository appointmentRepository,
        JpaAppointmentRepository jpaAppointmentRepository,
        IAppointmentMapper appointmentMapper,
        IFhirParser fhirParser) {

        this.appointmentRepository = appointmentRepository;
        this.jpaAppointmentRepository = jpaAppointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.fhirParser = fhirParser;
    }

    public List<AppointmentEntity> getAppointments(){
        return appointmentRepository.getAppointments();
    }

    public void saveAppointment(String fhirJson) {
        try {
            Appointment appointment = fhirParser.parseAppointment(fhirJson);
            String fhirId = appointment.getIdElement().getIdPart();

            AppointmentEntity existing = findAppointmentByFhirId(fhirId);
            if (existing != null && appointment.getStatus() == Appointment.AppointmentStatus.BOOKED) {
                throw new IllegalStateException("Appointment with FHIR ID " + fhirId + " is already booked");
            } else if (existing != null && appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
                updateAppointment(existing, appointment);
            } else {
                createAppointment(appointment);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to process FHIR JSON: " + e.getMessage());
        }
    }

    public AppointmentEntity findAppointmentByFhirId(String fhirId) {
        return appointmentRepository.findByAppointmentId(fhirId).orElse(null);
    }

    public boolean appointmentExists(String fhirId) {
        return appointmentRepository.findByAppointmentId(fhirId).isPresent();
    }

    private void createAppointment(Appointment appointment) {
        AppointmentEntity appointmentEntity = appointmentMapper.toEntity(appointment);
        if (appointmentEntity == null) {
            throw new IllegalArgumentException("Failed to convert FHIR Appointment to Appointment entity");
        }
        jpaAppointmentRepository.save(appointmentEntity);
    }

    private void updateAppointment(AppointmentEntity existing, Appointment appointment) {
        AppointmentEntity updated = appointmentMapper.toEntity(appointment);
        updated.setId(existing.getId());
        jpaAppointmentRepository.save(updated);
    }

    @Override
    public void processAppointmentEvent(String fhirJson) {
        saveAppointment(fhirJson);
    }
}
