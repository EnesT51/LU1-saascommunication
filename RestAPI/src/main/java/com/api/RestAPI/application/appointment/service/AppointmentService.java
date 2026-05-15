package com.api.RestAPI.application.appointment.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.api.RestAPI.application.HapiFhir.interfaces.IFhirParser;
import com.api.RestAPI.application.appointment.component.AppointmentStateHandler;
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

    public List<AppointmentEntity> getAppointments(){
        return appointmentRepository.getAppointments();
    }

    private AppointmentEntity saveAppointment(String fhirJson) {
        Appointment fhirAppointment = fhirParser.parseAppointment(fhirJson);
        String fhirId = fhirAppointment.getIdElement().getIdPart();

        AppointmentEntity newData = appointmentMapper.toEntity(fhirAppointment);
        Optional<AppointmentEntity> existingOpt = appointmentRepository.findByAppointmentId(fhirId);
        if (existingOpt.isEmpty()) {
            stateHandler.validateCreation(newData.getStatus());
            AppointmentEntity saved = createAppointment(newData);
            saved.setNewlyCreated(true);
            return saved;
        } else {
            AppointmentEntity existing = existingOpt.get();
            AppointmentStatus currentStatus = existing.getStatus();

            if (!newData.getStatus().equals(currentStatus)) {stateHandler.validateTransition(currentStatus, newData.getStatus());}
            AppointmentEntity updated = updateAppointment(existing, newData);
            updated.setNewlyCreated(false);
            return updated;
        }
    }
    private AppointmentEntity createAppointment(AppointmentEntity appointmentEntity) {
        return appointmentRepository.save(appointmentEntity);
    }

    private AppointmentEntity updateAppointment(AppointmentEntity existing, AppointmentEntity updated) {
        existing.setStatus(updated.getStatus());
        existing.setStart(updated.getStart());
        existing.setEnd(updated.getEnd());
        existing.setDescription(updated.getDescription());
        existing.setComment(updated.getComment());
        existing.setPatientId(updated.getPatientId());
        existing.setPatientName(updated.getPatientName());
        existing.setPractitionerId(updated.getPractitionerId());
        existing.setPractitionerName(updated.getPractitionerName());
        return appointmentRepository.save(existing);
    }

    @Override
    public AppointmentEntity processAppointmentEvent(String fhirJson) {
        return saveAppointment(fhirJson);
    }
}
