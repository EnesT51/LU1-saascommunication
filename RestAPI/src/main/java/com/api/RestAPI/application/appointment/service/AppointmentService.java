package com.api.RestAPI.application.appointment.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.api.RestAPI.application.HapiFhir.interfaces.IFhirParser;

import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentStateHandler;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import com.api.RestAPI.domain.appointment.enums.AppointmentStatus;

import jakarta.transaction.Transactional;

import org.hl7.fhir.r4.model.Appointment;

@Service
@Transactional
public class AppointmentService implements IAppointmentEventProcessor {

    private final IAppointmentRepository appointmentRepository;
    private final IAppointmentMapper appointmentMapper;
    private final IFhirParser fhirParser;
    private final IAppointmentStateHandler stateHandler;

    public AppointmentService(
            IAppointmentRepository appointmentRepository,
            IAppointmentMapper appointmentMapper,
            IFhirParser fhirParser,
            IAppointmentStateHandler stateHandler) {

        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.fhirParser = fhirParser;
        this.stateHandler = stateHandler;
    }

    public List<AppointmentEntity> getAppointments() {
        return appointmentRepository.getAppointments();
    }

    private AppointmentEntity saveAppointment(String fhirJson) {
        Appointment fhirAppointment = parseAppointment(fhirJson);

        String fhirId = extractFhirId(fhirAppointment);

        AppointmentEntity newData = appointmentMapper.toEntity(fhirAppointment);

        return appointmentRepository.findByAppointmentId(fhirId)
                .map(existing -> handleUpdate(existing, newData))
                .orElseGet(() -> handleCreate(newData));
    }

    private Appointment parseAppointment(String fhirJson) {
        return fhirParser.parseAppointment(fhirJson);
    }
    private String extractFhirId(Appointment appointment) {
        return appointment.getIdElement().getIdPart();
    }

    private AppointmentEntity handleCreate(AppointmentEntity newAppointment) {
        stateHandler.validateCreation(newAppointment.getStatus());

        AppointmentEntity saved = appointmentRepository.save(newAppointment);
        saved.setNewlyCreated(true);

        return saved;
    }

    private AppointmentEntity handleUpdate(AppointmentEntity existing, AppointmentEntity newData) {
        validateStatusTransition(existing, newData);
        updateAppointmentFields(existing, newData);
        AppointmentEntity updated = appointmentRepository.save(existing);
        updated.setNewlyCreated(false);
        return updated;
    }

    private void validateStatusTransition(AppointmentEntity existing, AppointmentEntity newData) {

        AppointmentStatus currentStatus = existing.getStatus();
        AppointmentStatus newStatus = newData.getStatus();

        if (!currentStatus.equals(newStatus)) {
            stateHandler.validateTransition(currentStatus, newStatus);
        }
    }

    private void updateAppointmentFields(AppointmentEntity existing, AppointmentEntity updated) {

        existing.setStatus(updated.getStatus());
        existing.setStart(updated.getStart());
        existing.setEnd(updated.getEnd());
        existing.setDescription(updated.getDescription());
        existing.setComment(updated.getComment());
        existing.setPatientId(updated.getPatientId());
        existing.setPatientName(updated.getPatientName());
        existing.setPractitionerId(updated.getPractitionerId());
        existing.setPractitionerName(updated.getPractitionerName());
    }

    @Override
    public AppointmentEntity processAppointmentEvent(String fhirJson) {
        return saveAppointment(fhirJson);
    }
}
