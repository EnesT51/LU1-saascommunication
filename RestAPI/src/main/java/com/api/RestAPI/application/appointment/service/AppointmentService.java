package com.api.RestAPI.application.appointment.service;

import org.springframework.stereotype.Service;

import java.util.List;

import com.api.RestAPI.application.HapiFhir.interfaces.IFhirParser;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import com.api.RestAPI.infrastructure.appointment.persistence.JpaAppointmentRepository;
import org.hl7.fhir.r4.model.Appointment;

@Service
public class AppointmentService {
    
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
            Appointment appointment = fhirParser.parseAppointment(fhirJson);
            AppointmentEntity appointmentEntity = appointmentMapper.toEntity(appointment, null);
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
