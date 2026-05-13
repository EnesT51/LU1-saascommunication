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
import org.openmrs.module.saascommunication.fhir.FhirAppointmentMapper;
import org.openmrs.module.saascommunication.messaging.ActiveMqPublisher;
import org.springframework.aop.AfterReturningAdvice;

public class AppointmentPublishAdvice implements AfterReturningAdvice {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	private final FhirAppointmentMapper appointmentMapper = new FhirAppointmentMapper();
	
	private final ActiveMqPublisher publisher = new ActiveMqPublisher();
	
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
		if (!"validateAndSave".equals(method.getName()) || returnValue == null) {
			return;
		}
		
		try {
			String payload = appointmentMapper.toFhirJson(returnValue);
			publisher.publishAppointment(payload);
			log.info("Published appointment " + getAppointmentIdentifier(returnValue) + " to ActiveMQ");
		}
		catch (Exception e) {
			log.error("Failed to publish appointment " + getAppointmentIdentifier(returnValue) + " to ActiveMQ", e);
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
