/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.saascommunication.fhir;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.Provider;

public class FhirAppointmentMapper {
	
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	
	public String toFhirJson(Object appointment) {
		StringBuilder json = new StringBuilder();
		json.append("{");
		appendField(json, "resourceType", "Appointment");
		appendField(json, "id", getAppointmentId(appointment));
		appendField(json, "status", mapStatus(appointment));
		appendServiceCategory(json, appointment);
		appendAppointmentType(json, appointment);
		appendReasonCodes(json, invokeCollection(appointment, "getReasons"));
		appendField(json, "description", getDescription(appointment));
		appendDateField(json, "start", invokeDate(appointment, "getStartDateTime"));
		appendDateField(json, "end", invokeDate(appointment, "getEndDateTime"));
		appendDateField(json, "created", invokeDate(appointment, "getDateCreated"));
		appendField(json, "comment", invokeString(appointment, "getComments"));
		appendParticipants(json, appointment);
		removeTrailingComma(json);
		json.append("}");
		return json.toString();
	}
	
	private void appendServiceCategory(StringBuilder json, Object appointment) {
		Object service = invoke(appointment, "getService");
		String serviceName = invokeString(service, "getName");
		if (isBlank(serviceName)) {
			return;
		}
		
		json.append("\"serviceCategory\":[{\"text\":\"").append(escape(serviceName)).append("\"}],");
	}
	
	private void appendAppointmentType(StringBuilder json, Object appointment) {
		Object serviceType = invoke(appointment, "getServiceType");
		String appointmentType = invokeString(serviceType, "getName");
		if (isBlank(appointmentType)) {
			appointmentType = enumName(invoke(appointment, "getStatus"));
		}
		if (isBlank(appointmentType)) {
			return;
		}
		
		json.append("\"appointmentType\":{\"text\":\"").append(escape(appointmentType)).append("\"},");
	}
	
	private void appendReasonCodes(StringBuilder json, Collection<?> reasons) {
		if (reasons == null || reasons.isEmpty()) {
			return;
		}
		
		List<String> values = new ArrayList<String>();
		for (Object reason : reasons) {
			Object concept = invoke(reason, "getConcept");
			String display = invokeString(concept, "getDisplayString");
			if (!isBlank(display)) {
				values.add(display);
			}
		}
		
		if (values.isEmpty()) {
			return;
		}
		
		json.append("\"reasonCode\":[");
		for (String value : values) {
			json.append("{\"text\":\"").append(escape(value)).append("\"},");
		}
		removeTrailingComma(json);
		json.append("],");
	}
	
	private void appendParticipants(StringBuilder json, Object appointment) {
		List<String> participants = new ArrayList<String>();
		
		String patientParticipant = buildPatientParticipant(invokePatient(appointment, "getPatient"));
		if (patientParticipant != null) {
			participants.add(patientParticipant);
		}
		
		String locationParticipant = buildLocationParticipant(invokeLocation(appointment, "getLocation"));
		if (locationParticipant != null) {
			participants.add(locationParticipant);
		}
		
		String providerParticipant = buildProviderParticipant(appointment);
		if (providerParticipant != null) {
			participants.add(providerParticipant);
		}
		
		if (participants.isEmpty()) {
			return;
		}
		
		json.append("\"participant\":[");
		for (String participant : participants) {
			json.append(participant).append(",");
		}
		removeTrailingComma(json);
		json.append("],");
	}
	
	private String buildPatientParticipant(Patient patient) {
		if (patient == null) {
			return null;
		}
		
		String referenceId = patient.getUuid() != null ? patient.getUuid() : getPatientIdentifier(patient);
		String display = getPatientDisplay(patient);
		if (isBlank(referenceId) && isBlank(display)) {
			return null;
		}
		
		return buildParticipant("Patient/" + defaultString(referenceId, "unknown"), display);
	}
	
	private String buildLocationParticipant(Location location) {
		if (location == null) {
			return null;
		}
		
		String referenceId = location.getUuid() != null ? location.getUuid() : String.valueOf(location.getLocationId());
		return buildParticipant("Location/" + referenceId, location.getName());
	}
	
	private String buildProviderParticipant(Object appointment) {
		Provider provider = invokeProvider(appointment, "getProvider");
		Collection<?> appointmentProviders = invokeCollection(appointment, "getProviders");
		
		if (provider == null && appointmentProviders != null) {
			for (Object appointmentProvider : appointmentProviders) {
				Provider candidate = invokeProvider(appointmentProvider, "getProvider");
				if (candidate != null) {
					provider = candidate;
					break;
				}
			}
		}
		
		if (provider == null) {
			return null;
		}
		
		String referenceId = provider.getUuid() != null ? provider.getUuid() : String.valueOf(provider.getProviderId());
		return buildParticipant("Practitioner/" + referenceId, provider.getName());
	}
	
	private String buildParticipant(String reference, String display) {
		StringBuilder participant = new StringBuilder();
		participant.append("{\"actor\":{");
		participant.append("\"reference\":\"").append(escape(reference)).append("\"");
		if (!isBlank(display)) {
			participant.append(",\"display\":\"").append(escape(display)).append("\"");
		}
		participant.append("},\"status\":\"accepted\"}");
		return participant.toString();
	}
	
	private void appendField(StringBuilder json, String name, String value) {
		if (isBlank(value)) {
			return;
		}
		
		json.append("\"").append(name).append("\":\"").append(escape(value)).append("\",");
	}
	
	private void appendDateField(StringBuilder json, String name, Date value) {
		if (value == null) {
			return;
		}
		
		json.append("\"").append(name).append("\":\"").append(formatDate(value)).append("\",");
	}
	
	private String getAppointmentId(Object appointment) {
		String uuid = invokeString(appointment, "getUuid");
		if (!isBlank(uuid)) {
			return uuid;
		}
		
		String appointmentNumber = invokeString(appointment, "getAppointmentNumber");
		if (!isBlank(appointmentNumber)) {
			return appointmentNumber;
		}
		
		Object appointmentId = invoke(appointment, "getAppointmentId");
		if (appointmentId != null) {
			return String.valueOf(appointmentId);
		}
		
		return "unknown-appointment";
	}
	
	private String getDescription(Object appointment) {
		Object service = invoke(appointment, "getService");
		String description = invokeString(service, "getDescription");
		if (!isBlank(description)) {
			return description;
		}
		return invokeString(service, "getName");
	}
	
	private String getPatientDisplay(Patient patient) {
		PersonName name = patient.getPersonName();
		if (name != null && !isBlank(name.getFullName())) {
			return name.getFullName();
		}
		return getPatientIdentifier(patient);
	}
	
	private String getPatientIdentifier(Patient patient) {
		PatientIdentifier identifier = patient.getPatientIdentifier();
		if (identifier != null && !isBlank(identifier.getIdentifier())) {
			return identifier.getIdentifier();
		}
		return null;
	}
	
	private String mapStatus(Object appointment) {
		String status = enumName(invoke(appointment, "getStatus"));
		if (status == null) {
			return "proposed";
		}
		if ("Scheduled".equals(status) || "CheckedIn".equals(status) || "Arrived".equals(status)) {
			return "booked";
		}
		if ("Completed".equals(status)) {
			return "fulfilled";
		}
		if ("Cancelled".equals(status)) {
			return "cancelled";
		}
		if ("Missed".equals(status)) {
			return "noshow";
		}
		return "proposed";
	}
	
	private String formatDate(Date value) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		formatter.setTimeZone(UTC);
		return formatter.format(value);
	}
	
	private void removeTrailingComma(StringBuilder value) {
		int lastIndex = value.length() - 1;
		if (lastIndex >= 0 && value.charAt(lastIndex) == ',') {
			value.deleteCharAt(lastIndex);
		}
	}
	
	private boolean isBlank(String value) {
		return value == null || value.trim().length() == 0;
	}
	
	private String defaultString(String value, String fallback) {
		return isBlank(value) ? fallback : value;
	}
	
	private Object invoke(Object target, String methodName) {
		if (target == null) {
			return null;
		}
		
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
	
	private Date invokeDate(Object target, String methodName) {
		Object value = invoke(target, methodName);
		return value instanceof Date ? (Date) value : null;
	}
	
	private Collection<?> invokeCollection(Object target, String methodName) {
		Object value = invoke(target, methodName);
		return value instanceof Collection<?> ? (Collection<?>) value : null;
	}
	
	private Patient invokePatient(Object target, String methodName) {
		Object value = invoke(target, methodName);
		return value instanceof Patient ? (Patient) value : null;
	}
	
	private Location invokeLocation(Object target, String methodName) {
		Object value = invoke(target, methodName);
		return value instanceof Location ? (Location) value : null;
	}
	
	private Provider invokeProvider(Object target, String methodName) {
		Object value = invoke(target, methodName);
		return value instanceof Provider ? (Provider) value : null;
	}
	
	private String enumName(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Enum<?>) {
			return ((Enum<?>) value).name();
		}
		return String.valueOf(value);
	}
	
	private String escape(String value) {
		StringBuilder escaped = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			switch (current) {
				case '\\':
					escaped.append("\\\\");
					break;
				case '"':
					escaped.append("\\\"");
					break;
				case '\n':
					escaped.append("\\n");
					break;
				case '\r':
					escaped.append("\\r");
					break;
				case '\t':
					escaped.append("\\t");
					break;
				default:
					escaped.append(current);
			}
		}
		return escaped.toString();
	}
}
