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

import org.openmrs.module.saascommunication.factory.SaasCommunicationFactory;
import org.openmrs.module.saascommunication.fhir.AppointmentMapper;
import org.openmrs.module.saascommunication.fhir.FhirMessageValidator;
import org.openmrs.module.saascommunication.messaging.AppointmentPublisher;
import org.openmrs.module.saascommunication.messaging.MessageTracker;

/**
 * Intercepteert de OpenMRS AppointmentService.validateAndSave() methode en publiceert
 * het resultaat als FHIR R4 Appointment naar ActiveMQ.
 *
 * Om een andere OpenMRS-module te koppelen (bijv. medische resultaten):
 *   Maak een nieuwe klasse die AbstractPublishAdvice uitbreidt en overschrijf
 *   getSupportedMethodName() en toFhirPayload(). Zie AbstractPublishAdvice voor details.
 */
public class AppointmentPublishAdvice extends AbstractPublishAdvice {

	private final AppointmentMapper appointmentMapper;

	public AppointmentPublishAdvice() {
		this(
		    SaasCommunicationFactory.createAppointmentMapper(),
		    SaasCommunicationFactory.createAppointmentPublisher(),
		    SaasCommunicationFactory.createFhirMessageValidator(),
		    SaasCommunicationFactory.createMessageTracker()
		);
	}

	public AppointmentPublishAdvice(AppointmentMapper appointmentMapper, AppointmentPublisher publisher,
	        FhirMessageValidator validator, MessageTracker tracker) {
		super(publisher, validator, tracker);
		this.appointmentMapper = appointmentMapper;
	}

	@Override
	protected String getSupportedMethodName() {
		return "validateAndSave";
	}

	@Override
	protected String toFhirPayload(Object returnValue) {
		return appointmentMapper.toFhirJson(returnValue);
	}

	@Override
	protected String getIdentifier(Object returnValue) {
		String uuid = invokeString(returnValue, "getUuid");
		if (uuid != null) {
			return uuid;
		}
		String appointmentNumber = invokeString(returnValue, "getAppointmentNumber");
		if (appointmentNumber != null) {
			return appointmentNumber;
		}
		Object appointmentId = invoke(returnValue, "getAppointmentId");
		return appointmentId != null ? String.valueOf(appointmentId) : "unknown-appointment";
	}
}
