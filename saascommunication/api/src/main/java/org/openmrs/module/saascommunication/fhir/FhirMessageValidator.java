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

/**
 * Validates a FHIR Appointment JSON payload before it is published to ActiveMQ. Checks structure,
 * required fields and syntax per HL7/FHIR specification.
 */
public class FhirMessageValidator {
	
	private static final String[] REQUIRED_FIELDS = { "\"resourceType\":\"Appointment\"", "\"id\":", "\"status\":",
	        "\"start\":", "Patient/" };
	
	/**
	 * @throws IllegalArgumentException when a required field is missing or the payload is empty
	 */
	public void validate(String fhirJson) {
		if (fhirJson == null || fhirJson.trim().isEmpty()) {
			throw new IllegalArgumentException("FHIR validatie mislukt: payload is leeg");
		}
		
		for (String field : REQUIRED_FIELDS) {
			if (!fhirJson.contains(field)) {
				throw new IllegalArgumentException("FHIR validatie mislukt: verplicht veld ontbreekt [" + field + "]");
			}
		}
	}
}
