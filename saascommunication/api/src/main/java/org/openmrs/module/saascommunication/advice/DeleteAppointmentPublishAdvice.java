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
 * Publiceert een cancel event voor OpenMRS appointments. In de OpenMRS AppointmentsService is er
 * geen deleteAppointment()-methode; annuleren gebeurt via changeStatus(Appointment, String, Date).
 * Daarom koppelen we hier op statuswijzigingen naar CANCELLED.
 */
public class DeleteAppointmentPublishAdvice extends AbstractPublishAdvice {
	
	private static final String[] SUPPORTED_METHOD_NAMES = { "changeStatus" };
	
	private static final String STATUS_CANCELLED = "cancelled";
	
	private static final String STATUS_CANCELED = "canceled";
	
	private final AppointmentMapper appointmentMapper;
	
	public DeleteAppointmentPublishAdvice() {
		this(SaasCommunicationFactory.createAppointmentMapper(), SaasCommunicationFactory.createAppointmentPublisher(),
		        SaasCommunicationFactory.createFhirMessageValidator(), SaasCommunicationFactory.createMessageTracker());
	}
	
	public DeleteAppointmentPublishAdvice(AppointmentMapper appointmentMapper, AppointmentPublisher publisher,
	    FhirMessageValidator validator, MessageTracker tracker) {
		super(publisher, validator, tracker);
		this.appointmentMapper = appointmentMapper;
	}
	
	@Override
	protected String[] getSupportedMethodNames() {
		return SUPPORTED_METHOD_NAMES;
	}
	
	@Override
	protected Object resolvePayloadSource(Object returnValue, Object[] args) {
		if (args == null || args.length < 2) {
			return null;
		}
		String status = args[1] != null ? String.valueOf(args[1]) : null;
		if (status == null) {
			return null;
		}
		if (!STATUS_CANCELLED.equalsIgnoreCase(status) && !STATUS_CANCELED.equalsIgnoreCase(status)) {
			return null;
		}
		return args[0];
	}
	
	@Override
	protected String toFhirPayload(Object returnValue) {
		return appointmentMapper.toCancelledFhirJson(returnValue);
	}
}
