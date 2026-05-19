package org.openmrs.module.saascommunication.factory;

import org.openmrs.module.saascommunication.advice.AppointmentPublishAdvice;
import org.openmrs.module.saascommunication.fhir.AppointmentMapper;
import org.openmrs.module.saascommunication.fhir.FhirAppointmentMapper;
import org.openmrs.module.saascommunication.messaging.ActiveMqPublisher;
import org.openmrs.module.saascommunication.messaging.AppointmentPublisher;

public final class SaasCommunicationFactory {
	
	private SaasCommunicationFactory() {
	}
	
	public static AppointmentPublishAdvice createAppointmentPublishAdvice() {
		return new AppointmentPublishAdvice(createAppointmentMapper(), createAppointmentPublisher());
	}
	
	public static AppointmentMapper createAppointmentMapper() {
		return new FhirAppointmentMapper();
	}
	
	public static AppointmentPublisher createAppointmentPublisher() {
		return new ActiveMqPublisher();
	}
}
