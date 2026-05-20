/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.saascommunication.messaging;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tracks each outbound FHIR message and logs HL7-style ACK responses. ACK:AA = Application Accept
 * (message successfully published) ACK:AE = Application Error (publish failed after all retries)
 */
public class MessageTracker {
	
	private final Log log = LogFactory.getLog(getClass());
	
	public String generateMessageId() {
		return UUID.randomUUID().toString();
	}
	
	public void logSending(String messageId, String appointmentId) {
		log.info("[TRACKING] messageId=" + messageId + " appointmentId=" + appointmentId + " status=SENDING");
	}
	
	public void logAckAccept(String messageId, String appointmentId) {
		log.info("[ACK:AA] messageId=" + messageId + " appointmentId=" + appointmentId
		        + " status=ACCEPTED message published successfully");
	}
	
	public void logAckError(String messageId, String appointmentId, int attempt, Throwable cause) {
		log.warn("[ACK:AE] messageId=" + messageId + " appointmentId=" + appointmentId + " attempt=" + attempt
		        + " status=ERROR reason=" + cause.getMessage());
	}
	
	public void logValidationFailure(String appointmentId, String reason) {
		log.error("[VALIDATION] appointmentId=" + appointmentId + " status=REJECTED reason=" + reason);
	}
}
