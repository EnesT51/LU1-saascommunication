package com.api.RestAPI.application.appointment.service;

import org.springframework.stereotype.Service;

import java.util.List;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import com.api.RestAPI.infrastructure.appointment.persistence.JpaAppointmentRepository;
import com.api.RestAPI.application.appointment.dto.AppointmentDto;
import com.api.RestAPI.domain.appointment.abstractRepository.AppointmentRepository;
import com.api.RestAPI.application.appointment.mapper.AppointmentMapper;
import org.hl7.fhir.r4.model.Appointment;
import com.api.RestAPI.infrastructure.config.FhirConfig;

import ca.uhn.fhir.parser.IParser;
@Service
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final JpaAppointmentRepository jpaAppointmentRepository;
    private final FhirConfig fhirConfig;

    public AppointmentService(AppointmentRepository appointmentRepository, JpaAppointmentRepository jpaAppointmentRepository, FhirConfig fhirConfig) {
        this.appointmentRepository = appointmentRepository;
        this.jpaAppointmentRepository = jpaAppointmentRepository;
        this.fhirConfig = fhirConfig;
    }

    public List<AppointmentEntity> getAppointments(){
        return appointmentRepository.getAppointments();
    }
    // public void createAppointment(AppointmentDto appointment){
    //     // Convert DTO to entity if necessary
    //     this.CheckIfAppointmentExists(appointment);
    //     AppointmentEntity appointmentEntity = AppointmentMapper.toEntity(appointment);
    //     if (appointmentEntity == null) {
    //         throw new IllegalArgumentException("Failed to convert AppointmentDto to Appointment entity");
    //     }
    //     // Set properties from DTO to entity
    //     jpaAppointmentRepository.save(appointmentEntity);
    // }

    public void receiveAppointment(String fhirJson) {
        
        try{
            IParser parser = fhirConfig.fhirContext().newJsonParser();
            Appointment appointment = parser.parseResource(Appointment.class, fhirJson);
            if (appointment == null) {
                throw new IllegalArgumentException("Failed to parse FHIR JSON into Appointment resource");
            }
            AppointmentEntity appointmentEntity = AppointmentMapper.toEntity(appointment);
            if (appointmentEntity == null) {
                throw new IllegalArgumentException("Failed to convert FHIR Appointment to Appointment entity");
            }
            jpaAppointmentRepository.save(appointmentEntity);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse FHIR JSON: " + e.getMessage());
        }
    }

    // private void CheckIfAppointmentExists(AppointmentDto appointment) {
    //     // Implement logic to check if the appointment already exists
    //     if(appointment == null) {
    //         throw new IllegalArgumentException("Appointment cannot be null");
    //     }
    // }
}
