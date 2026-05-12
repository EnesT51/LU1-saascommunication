package com.api.RestAPI.application.appointment.mapper;

import com.api.RestAPI.application.appointment.dto.AppointmentDto;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import com.api.RestAPI.domain.appointment.enums.AppointmentStatus;
public class AppointmentMapper {
    
    // public static AppointmentEntity toEntity(AppointmentDto appointmentDto) {
    //     if (appointmentDto == null) {
    //         return null;
    //     }
    //     AppointmentEntity appointment = new AppointmentEntity();
    //     appointment.setAppointmentId(appointmentDto.getAppointmentId());
    //     appointment.setPatientId(appointmentDto.getPatientId());
    //     appointment.setPhoneNumber(appointmentDto.getPhoneNumber());
    //     appointment.setStart(appointmentDto.getStart());
    //     appointment.setEnd(appointmentDto.getEnd());
    //     appointment.setInstructions(appointmentDto.getInstructions());
    //     appointment.setLocation(appointmentDto.getLocation());
    //     appointment.setStatus(AppointmentStatus.valueOf(appointmentDto.getStatus().toString()));
    //     return appointment;
    // }
    public static AppointmentEntity toEntity(org.hl7.fhir.r4.model.Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        AppointmentEntity appointmentEntity = new AppointmentEntity();
        // Map fields from FHIR Appointment to your AppointmentEntity
        // This is a simplified example, you may need to handle more fields and complex mappings
        appointmentEntity.setAppointmentId(appointment.getIdElement().getIdPart());
        appointmentEntity.setStart(appointment.getStart());
        appointmentEntity.setEnd(appointment.getEnd());
        appointmentEntity.setInstructions(appointment.getDescription());
        // Map status from FHIR to your enum
        if (appointment.hasStatus()) {
            switch (appointment.getStatus()) {
                case BOOKED:
                    appointmentEntity.setStatus(AppointmentStatus.BOOKED);
                    break;
                case CANCELLED:
                    appointmentEntity.setStatus(AppointmentStatus.CANCELLED);
                    break;
                default:
                    appointmentEntity.setStatus(AppointmentStatus.BOOKED); // Default mapping
            }
        }
        return appointmentEntity;
    }
}
