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
import org.openmrs.module.saascommunication.fhir.FhirMessageValidator;
import org.openmrs.module.saascommunication.messaging.AppointmentPublisher;
import org.openmrs.module.saascommunication.messaging.MessageTracker;
import org.springframework.aop.AfterReturningAdvice;

/**
 * Generieke basisklasse voor AOP-advices die OpenMRS-events als FHIR-bericht publiceren. Hoe een
 * nieuwe module dit uitbreidt: 1. Maak een subklasse aan 2. Override getSupportedMethodNames() met
 * de methode(s) die je wilt intercepten 3. Override toFhirPayload() als de FHIR-mapping afwijkt van
 * de standaard 4. Registreer de advice in config.xml van de module
 */
public abstract class AbstractPublishAdvice implements AfterReturningAdvice {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected final AppointmentPublisher publisher;
	
	protected final FhirMessageValidator validator;
	
	protected final MessageTracker tracker;
	
	protected AbstractPublishAdvice(AppointmentPublisher publisher, FhirMessageValidator validator, MessageTracker tracker) {
		this.publisher = publisher;
		this.validator = validator;
		this.tracker = tracker;
	}
	
	/**
	 * De namen van de OpenMRS-servicemethoden die geïntercepteerd moeten worden. Subklassen kunnen
	 * hier meerdere methoden opgeven, bijvoorbeeld create/update/delete.
	 */
	protected abstract String[] getSupportedMethodNames();
	
	/**
	 * Bepaalt welk object als basis voor het FHIR-bericht gebruikt wordt. Standaard is dat de
	 * returnValue van de geïntercepteerde methode.
	 */
	protected Object resolvePayloadSource(Object returnValue, Object[] args) {
		return returnValue;
	}
	
	/**
	 * Zet het returnValue om naar een FHIR-JSON payload. Subklassen overschrijven dit als ze een
	 * andere mapper nodig hebben.
	 */
	protected abstract String toFhirPayload(Object returnValue);
	
	/**
	 * Haal de identifier op van het object (UUID, appointmentNumber, of id). Subklassen kunnen dit
	 * overschrijven voor domeinspecifieke ID-extractie.
	 */
	protected String getIdentifier(Object returnValue) {
		String uuid = invokeString(returnValue, "getUuid");
		if (uuid != null) {
			return uuid;
		}
		Object id = invoke(returnValue, "getId");
		return id != null ? String.valueOf(id) : "unknown";
	}
	
	@Override
	public final void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
		if (method == null || !supportsMethod(method.getName())) {
			return;
		}
		
		Object payloadSource = resolvePayloadSource(returnValue, args);
		if (payloadSource == null) {
			return;
		}
		
		String entityId = getIdentifier(payloadSource);
		String messageId = tracker.generateMessageId();
		
		try {
			String payload = toFhirPayload(payloadSource);
			
			validator.validate(payload);
			tracker.logSending(messageId, entityId);
			publisher.publishAppointment(messageId, payload);
			tracker.logAckAccept(messageId, entityId);
		}
		catch (IllegalArgumentException validationException) {
			tracker.logValidationFailure(entityId, validationException.getMessage());
			log.error("FHIR validatie mislukt voor " + entityId + ": " + validationException.getMessage());
		}
		catch (Exception publishException) {
			log.error(
			    "Publicatie mislukt voor " + entityId + " (messageId=" + messageId + "): " + publishException.getMessage(),
			    publishException);
		}
	}
	
	private boolean supportsMethod(String methodName) {
		if (methodName == null || methodName.trim().isEmpty()) {
			return false;
		}
		
		String[] supportedMethodNames = getSupportedMethodNames();
		if (supportedMethodNames == null || supportedMethodNames.length == 0) {
			return false;
		}
		
		for (String supportedMethodName : getSupportedMethodNames()) {
			if (supportedMethodName != null && supportedMethodName.equals(methodName)) {
				return true;
			}
		}
		return false;
	}
	
	protected Object invoke(Object target, String methodName) {
		try {
			Method method = target.getClass().getMethod(methodName);
			return method.invoke(target);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	protected String invokeString(Object target, String methodName) {
		Object value = invoke(target, methodName);
		return value != null ? String.valueOf(value) : null;
	}
}
