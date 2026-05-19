package org.openmrs.module.saascommunication.messaging;

public interface AppointmentPublisher {
	
	void publishAppointment(String payload) throws Exception;
}
