package com.api.RestAPI.application.appointment.mapper;

import org.hibernate.MappingException;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;

import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;


@Component
public class AppointmentMapper implements IAppointmentMapper {

    public AppointmentEntity toEntity(Appointment appointment) {
        if (appointment == null) {
            throw new MappingException("Appointment mag niet null zijn");
        }
        AppointmentEntity appointmentEntity = new AppointmentEntity();

        appointmentEntity.setAppointmentId(appointment.getIdElement().getIdPart());
        appointmentEntity.setStart(appointment.getStart().toInstant());
        appointmentEntity.setEnd(appointment.getEnd().toInstant());
        appointmentEntity.setDescription(appointment.getDescription());
        appointmentEntity.setComment(appointment.getComment());
        appointmentEntity.prePersist();

        if (appointment.hasStatus()) { appointmentEntity.setStatus(mapStatus(appointment.getStatus())); }

        extractParticipants(appointmentEntity, appointment);
        return appointmentEntity;
    }

    public AppointmentResponseDto toDto(AppointmentEntity appointmentEntity) {
        if (appointmentEntity == null) {
            throw new MappingException("AppointmentEntity mag niet null zijn");
        }
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.toDto(appointmentEntity);
        return dto;
    }

    private AppointmentStatus mapStatus(AppointmentStatus status) {

        switch (status) {
            case BOOKED: return AppointmentStatus.BOOKED;
            case CANCELLED: return AppointmentStatus.CANCELLED;
            default: throw new MappingException("Onbekende status: " + status);
        }        
    }
    private void extractParticipants(AppointmentEntity appointmentEntity, Appointment appointment) {

        for (AppointmentParticipantComponent participant : appointment.getParticipant()) {

            if (!participant.hasActor()) {
                continue;
            }

            Reference actorRef = participant.getActor();

            String reference = actorRef.getReference();

            if (reference == null || reference.isBlank()) {
                continue;
            }

            String display = actorRef.getDisplay();

            String[] parts = reference.split("/");

            if (parts.length != 2) {
                continue;
            }

            String resourceType = parts[0];
            String resourceId = parts[1];

            switch (resourceType) {

                case "Patient":
                    appointmentEntity.setPatientId(resourceId);

                    if (display != null) {
                        appointmentEntity.setPatientName(display);
                    }
                    break;

                case "Practitioner":
                    appointmentEntity.setPractitionerId(resourceId);

                    if (display != null) {
                        appointmentEntity.setPractitionerName(display);
                    }
                    break;

                case "Location":
                    appointmentEntity.setLocationId(resourceId);

                    if (display != null) {
                        appointmentEntity.setLocation(display);
                    }
                    break;

                default:
                    break;
            }
        }
        extractExtensions(appointmentEntity, appointment);
    }

    private void extractExtensions(AppointmentEntity appointmentEntity, Appointment appointment) {

        for (Extension extension : appointment.getExtension()) {

            String url = extension.getUrl();

            if (url == null) {
                continue;
            }

            if ("http://saascommunication.openmrs.org/fhir/StructureDefinition/patientPhone".equals(url)
                    && extension.getValue() instanceof StringType stringType) {

                appointmentEntity.setPatientPhoneNumber(stringType.getValue());
            }

            if ("http://saascommunication.openmrs.org/fhir/StructureDefinition/organizationId".equals(url)
                    && extension.getValue() instanceof StringType stringType) {

                appointmentEntity.setOrganizationId(stringType.getValue());
            }
        }
    }
}
