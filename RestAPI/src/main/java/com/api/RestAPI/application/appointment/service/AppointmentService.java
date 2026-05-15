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

    private String saveAppointment(String fhirJson) {
        Appointment fhirAppointment = fhirParser.parseAppointment(fhirJson);
    
        String fhirId = fhirAppointment.getIdElement().getIdPart();
        AppointmentEntity appointmentEntity = appointmentMapper.toEntity(fhirAppointment);
        Optional<AppointmentEntity> existingAppointment = appointmentRepository.findByAppointmentId(fhirId);
        AppointmentStatus currentStatus = existingAppointment.map(AppointmentEntity::getStatus).orElse(null);

        if(existingAppointment.isEmpty()){
            stateHandler.validateCreation(appointmentEntity.getStatus());
            createAppointment(fhirAppointment);
            return "Afspraak met ID " + fhirId + " aangemaakt";
        } else {
            if(!appointmentEntity.getStatus().equals(existingAppointment.get().getStatus())) {
                stateHandler.validateTransition(currentStatus, appointmentEntity.getStatus());
            }
            updateAppointment(existingAppointment.get(), fhirAppointment);
            return "Afspraak met ID " + fhirId + " bijgewerkt";
        }
    }
    private void createAppointment(Appointment appointment) {
        AppointmentEntity appointmentEntity = appointmentMapper.toEntity(appointment);
        appointmentRepository.save(appointmentEntity);
    }

    private void updateAppointment(AppointmentEntity existing, Appointment appointment) {
        AppointmentEntity updated = appointmentMapper.toEntity(appointment);
        updated.setId(existing.getId());
        appointmentRepository.save(updated);
    }

    @Override
    public String processAppointmentEvent(String fhirJson) {
        return saveAppointment(fhirJson);
    }
}
