package com.api.RestAPI.infrastructure.appointment.persistence;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;

@Repository
public interface JpaAppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    
}
