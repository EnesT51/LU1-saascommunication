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
import org.openmrs.api.context.Context;

public class FhirAppointmentMapper implements AppointmentMapper {
	
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	
	public String toFhirJson(Object appointment) {
		return toFhirJson(appointment, null);
	}
	
	public String toCancelledFhirJson(Object appointment) {
		return toFhirJson(appointment, "cancelled");
	}
	
	private String toFhirJson(Object appointment, String forcedStatus) {
		StringBuilder json = new StringBuilder();
		json.append("{");
		appendField(json, "resourceType", "Appointment");
		appendField(json, "id", getAppointmentId(appointment));
		appendField(json, "status", mapStatus(appointment, forcedStatus));
		appendDateField(json, "start", invokeDate(appointment, "getStartDateTime"));
		appendDateField(json, "end", invokeDate(appointment, "getEndDateTime"));
		appendDateField(json, "created", invokeDate(appointment, "getDateCreated"));
		appendField(json, "comment", invokeString(appointment, "getComments"));
		appendPatientContactExtensions(json, appointment);
		appendParticipants(json, appointment);
		removeTrailingComma(json);
		json.append("}");
		return json.toString();
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
		if (isBlank(referenceId)) {
			return null;
		}
		
		return buildParticipant("Patient/" + referenceId, null);
	}
	
	private String buildLocationParticipant(Location location) {
		if (location == null) {
			return null;
		}
		
		String referenceId = location.getUuid() != null ? location.getUuid() : String.valueOf(location.getLocationId());
		return buildParticipant("Location/" + referenceId, location.getName());
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
	
	private void appendPatientContactExtensions(StringBuilder json, Object appointment) {
		// Haal telefoonnummer op — alleen mogelijk als er een patiënt is
		Patient patient = invokePatient(appointment, "getPatient");
		String phone = patient != null ? getPersonAttribute(patient, "Phone Number", "Telephone Number", "Mobile Number")
		        : null;
		
		// OrganisatieId en providerType komen altijd mee, ongeacht of er een patiënt is
		String organizationId = getGlobalProperty("saascommunication.organizationId");
		
		// Geen enkele extensie beschikbaar — niets toevoegen
		if (isBlank(phone) && isBlank(organizationId)) {
			return;
		}
		
		json.append("\"extension\":[");
		boolean hasEntry = false;
		
		if (!isBlank(phone)) {
			json.append(
			    "{\"url\":\"http://saascommunication.openmrs.org/fhir/StructureDefinition/patientPhone\",\"valueString\":\"")
			        .append(escape(phone)).append("\"}");
			hasEntry = true;
		}
		
		if (!isBlank(organizationId)) {
			if (hasEntry) {
				json.append(",");
			}
			json.append(
			    "{\"url\":\"http://saascommunication.openmrs.org/fhir/StructureDefinition/organizationId\",\"valueString\":\"")
			        .append(escape(organizationId)).append("\"}");
		}
		
		json.append("],");
	}
	
	private String getGlobalProperty(String property) {
		try {
			return Context.getAdministrationService().getGlobalProperty(property);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	private String getPersonAttribute(Patient patient, String... attributeTypeNames) {
		Collection<?> attributes = invokeCollection(patient, "getActiveAttributes");
		if (attributes == null) {
			attributes = invokeCollection(patient, "getAttributes");
		}
		if (attributes == null) {
			return null;
		}
		for (Object attr : attributes) {
			Object attributeType = invoke(attr, "getAttributeType");
			if (attributeType == null) {
				continue;
			}
			String typeName = invokeString(attributeType, "getName");
			if (typeName == null) {
				continue;
			}
			for (String sought : attributeTypeNames) {
				if (sought.equalsIgnoreCase(typeName)) {
					return invokeString(attr, "getValue");
				}
			}
		}
		return null;
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
	
	private String getPatientIdentifier(Patient patient) {
		PatientIdentifier identifier = patient.getPatientIdentifier();
		if (identifier != null && !isBlank(identifier.getIdentifier())) {
			return identifier.getIdentifier();
		}
		return null;
	}
	
	private String mapStatus(Object appointment) {
		return mapStatus(appointment, null);
	}
	
	private String mapStatus(Object appointment, String forcedStatus) {
		if (!isBlank(forcedStatus)) {
			return forcedStatus;
		}
		
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
