package com.api.RestAPI.application.appointment.mapper;

import org.hibernate.MappingException;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;

import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import com.api.RestAPI.domain.appointment.enums.AppointmentStatus;

@Component
public class AppointmentMapper implements IAppointmentMapper {

    public AppointmentEntity toEntity(Appointment appointment) {
        if (appointment == null) {
            throw new MappingException("Appointment mag niet null zijn");
        }
        AppointmentEntity appointmentEntity = new AppointmentEntity();

        appointmentEntity.setAppointmentId(appointment.getIdElement().getIdPart());
        appointmentEntity.setStart(appointment.getStart());
        appointmentEntity.setEnd(appointment.getEnd());
        appointmentEntity.setDescription(appointment.getDescription());
        appointmentEntity.setComment(appointment.getComment());

        if (appointment.hasStatus()) { appointmentEntity.setStatus(mapStatus(appointment.getStatus())); }

        extractParticipants(appointmentEntity, appointment);
        return appointmentEntity;
    }

    public AppointmentResponseDto toDto(AppointmentEntity appointmentEntity) {
        if (appointmentEntity == null) {
            throw new MappingException("AppointmentEntity mag niet null zijn");
        }
        AppointmentResponseDto dto = new AppointmentResponseDto();

        dto.setAppointmentId(appointmentEntity.getAppointmentId());
        dto.setStart(appointmentEntity.getStart());
        dto.setEnd(appointmentEntity.getEnd());
        dto.setDescription(appointmentEntity.getDescription());
        dto.setComment(appointmentEntity.getComment());
        dto.setStatus(appointmentEntity.getStatus().name());
        dto.setPatientId(appointmentEntity.getPatientId());
        dto.setPatientName(appointmentEntity.getPatientName());
        dto.setPractitionerId(appointmentEntity.getPractitionerId());
        dto.setPractitionerName(appointmentEntity.getPractitionerName());

        return dto;
    }

    private AppointmentStatus mapStatus(Appointment.AppointmentStatus status) {

        switch (status) {
            case BOOKED: return AppointmentStatus.BOOKED;
            case CANCELLED: return AppointmentStatus.CANCELLED;
            default: throw new MappingException("Onbekende status: " + status);
        }        
    }
    private void extractParticipants(AppointmentEntity appointmentEntity, Appointment appointment) {
        for (Appointment.AppointmentParticipantComponent participant : appointment.getParticipant()) {
            if (!participant.hasActor()) {
                continue;
            }

            Reference actorRef = participant.getActor();
            String reference = actorRef.getReference();
            String display = actorRef.getDisplay();

            if (reference != null) {
                if (reference.startsWith("Patient/")) {
                    appointmentEntity.setPatientId(actorRef.getIdElement().toString());
                    if (display != null) {
                        appointmentEntity.setPatientName(display);
                    }
                } else if (reference.startsWith("Practitioner/")) {
                    appointmentEntity.setPractitionerId(actorRef.getIdElement().toString());
                    if (display != null) {
                        appointmentEntity.setPractitionerName(display);
                    }
                }
            }
        }
    }
}
