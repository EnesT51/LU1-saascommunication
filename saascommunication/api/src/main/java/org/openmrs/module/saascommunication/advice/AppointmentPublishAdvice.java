/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.saascommunication.advice;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.saascommunication.factory.SaasCommunicationFactory;
import org.openmrs.module.saascommunication.fhir.AppointmentMapper;
import org.openmrs.module.saascommunication.fhir.FhirMessageValidator;
import org.openmrs.module.saascommunication.messaging.AppointmentPublisher;
import org.openmrs.module.saascommunication.messaging.MessageTracker;
import org.springframework.aop.AfterReturningAdvice;

public class AppointmentPublishAdvice implements AfterReturningAdvice {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private final AppointmentMapper appointmentMapper;
	
	private final AppointmentPublisher publisher;
	
	private final FhirMessageValidator validator;
	
	private final MessageTracker tracker;
	
	public AppointmentPublishAdvice() {
		this(SaasCommunicationFactory.createAppointmentMapper(), SaasCommunicationFactory.createAppointmentPublisher(),
		        SaasCommunicationFactory.createFhirMessageValidator(), SaasCommunicationFactory.createMessageTracker());
	}
	
	public AppointmentPublishAdvice(AppointmentMapper appointmentMapper, AppointmentPublisher publisher,
	    FhirMessageValidator validator, MessageTracker tracker) {
		this.appointmentMapper = appointmentMapper;
		this.publisher = publisher;
		this.validator = validator;
		this.tracker = tracker;
	}
	
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
		if (!"validateAndSave".equals(method.getName()) || returnValue == null) {
			return;
		}
		
		String appointmentId = getAppointmentIdentifier(returnValue);
		String messageId = tracker.generateMessageId();
		
		try {
			String payload = appointmentMapper.toFhirJson(returnValue);
			
			// HL7-eis: validatie van structuur en verplichte velden
			validator.validate(payload);
			
			// HL7-eis: logging/tracking — bericht wordt verstuurd
			tracker.logSending(messageId, appointmentId);
			
			// HL7-eis: queueing via ActiveMQ met retry
			publisher.publishAppointment(messageId, payload);
			
			// HL7-eis: ACK:AA — bericht succesvol geaccepteerd
			tracker.logAckAccept(messageId, appointmentId);
			
		}
		catch (IllegalArgumentException validationException) {
			// Validatie mislukt — niet versturen
			tracker.logValidationFailure(appointmentId, validationException.getMessage());
			log.error("FHIR validatie mislukt voor afspraak " + appointmentId + ": " + validationException.getMessage());
		}
		catch (Exception publishException) {
			// ACK:AE wordt per poging gelogd in de publisher; hier loggen we het eindresultaat
			log.error("Afspraak " + appointmentId + " (messageId=" + messageId
			        + ") kon niet worden gepubliceerd na alle pogingen: " + publishException.getMessage(), publishException);
		}
	}
	
	private String getAppointmentIdentifier(Object appointment) {
		String uuid = invokeString(appointment, "getUuid");
		if (uuid != null) {
			return uuid;
		}
		String appointmentNumber = invokeString(appointment, "getAppointmentNumber");
		if (appointmentNumber != null) {
			return appointmentNumber;
		}
		Object appointmentId = invoke(appointment, "getAppointmentId");
		return appointmentId != null ? String.valueOf(appointmentId) : "unknown-appointment";
	}
	
	private Object invoke(Object target, String methodName) {
		try {
			Method method = target.getClass().getMethod(methodName);
			return method.invoke(target);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	private String invokeString(Object target, String methodName) {
		Object value = invoke(target, methodName);
		return value != null ? String.valueOf(value) : null;
	}
}
