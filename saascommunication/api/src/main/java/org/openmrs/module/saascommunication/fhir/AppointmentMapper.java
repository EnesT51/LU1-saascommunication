package org.openmrs.module.saascommunication.fhir;

public interface AppointmentMapper {
	
	String toFhirJson(Object appointment);
}
