package com.api.RestAPI.application.appointment.mapper;


import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import com.api.RestAPI.domain.appointment.enums.AppointmentStatus;

@Component
public class AppointmentMapper implements IAppointmentMapper {
    
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
    public AppointmentEntity toEntity(Appointment appointment, Patient patient) {
        if (appointment == null) {
            return null;
        }
        AppointmentEntity appointmentEntity = new AppointmentEntity();

        appointmentEntity.setAppointmentId(appointment.getIdElement().getIdPart());
        appointmentEntity.setStart(appointment.getStart());
        appointmentEntity.setEnd(appointment.getEnd());
        appointmentEntity.setInstructions(appointment.getDescription());

        if (appointment.hasStatus()) {
            switch (appointment.getStatus()) {
                case BOOKED:
                    appointmentEntity.setStatus(AppointmentStatus.BOOKED);
                    break;
                case CANCELLED:
                    appointmentEntity.setStatus(AppointmentStatus.CANCELLED);
                    break;
                default:
                    appointmentEntity.setStatus(AppointmentStatus.BOOKED);
            }
        }
        return appointmentEntity;
    }
}
