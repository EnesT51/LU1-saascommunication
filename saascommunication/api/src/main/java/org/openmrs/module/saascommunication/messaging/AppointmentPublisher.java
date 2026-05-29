package org.openmrs.module.saascommunication.messaging;

public interface AppointmentPublisher {
	
	void publishAppointment(String messageId, String payload) throws Exception;
}
