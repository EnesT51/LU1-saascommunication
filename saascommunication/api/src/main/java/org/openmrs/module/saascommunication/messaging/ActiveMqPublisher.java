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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Publishes FHIR Appointment messages to ActiveMQ with retry and exponential backoff. Falls back to
 * HTTP web API when all JMS attempts fail.
 */
public class ActiveMqPublisher implements AppointmentPublisher {
	
	private static final int MAX_JMS_RETRIES = 3;
	
	private static final long BASE_DELAY_MS = 1000;
	
	private final MessageTracker tracker;
	
	private final String brokerUrl;
	
	private final String queueName;
	
	private final String webApiUrl;
	
	private final String webApiKey;
	
	public ActiveMqPublisher(MessageTracker tracker) {
		this.tracker = tracker;
		this.brokerUrl = getRequiredEnv("ACTIVEMQ_BROKER_URL");
		this.queueName = getRequiredEnv("ACTIVEMQ_QUEUE_NAME");
		this.webApiUrl = getRequiredEnv("ACTIVEMQ_WEB_API_URL");
		this.webApiKey = getRequiredEnv("RESTAPI_API_KEY");
	}
	
	@Override
	public void publishAppointment(String messageId, String payload) throws Exception {
		Exception lastJmsException = null;
		
		for (int attempt = 1; attempt <= MAX_JMS_RETRIES; attempt++) {
			try {
				publishViaJms(payload);
				return;
			}
			catch (Exception e) {
				lastJmsException = e;
				tracker.logAckError(messageId, extractId(payload), attempt, e);
				
				if (attempt < MAX_JMS_RETRIES) {
					sleepWithBackoff(attempt);
				}
			}
		}
		
		// All JMS retries exhausted — fall back to HTTP
		try {
			publishViaWebApi(payload);
		}
		catch (Exception httpException) {
			throw new Exception("Publish mislukt na " + MAX_JMS_RETRIES
			        + " JMS pogingen en HTTP fallback. Laatste JMS fout: " + lastJmsException.getMessage(), httpException);
		}
	}
	
	private void publishViaJms(String payload) throws Exception {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
		Thread currentThread = Thread.currentThread();
		ClassLoader originalContextClassLoader = currentThread.getContextClassLoader();
		
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		
		try {
			currentThread.setContextClassLoader(ActiveMQConnectionFactory.class.getClassLoader());
			
			connection = connectionFactory.createConnection();
			connection.start();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			Destination destination = session.createQueue(queueName);
			producer = session.createProducer(destination);
			
			TextMessage message = session.createTextMessage(payload);
			producer.send(message);
		}
		finally {
			currentThread.setContextClassLoader(originalContextClassLoader);
			
			if (producer != null) {
				producer.close();
			}
			if (session != null) {
				session.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
	}
	
	private void publishViaWebApi(String payload) throws Exception {
		HttpURLConnection connection = null;
		
		try {
			URL url = new URL(webApiUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/fhir+json");
			connection.setRequestProperty("X-API-KEY", webApiKey);
			
			byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
			connection.setFixedLengthStreamingMode(payloadBytes.length);
			
			OutputStream outputStream = connection.getOutputStream();
			try {
				outputStream.write(payloadBytes);
			}
			finally {
				outputStream.close();
			}
			
			int responseCode = connection.getResponseCode();
			if (responseCode < 200 || responseCode >= 300) {
				throw new IllegalStateException("HTTP fallback gaf status " + responseCode);
			}
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	private void sleepWithBackoff(int attempt) {
		try {
			long delay = BASE_DELAY_MS * (1L << (attempt - 1));
			Thread.sleep(delay);
		}
		catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}
	
	private String extractId(String payload) {
		int idIdx = payload.indexOf("\"id\":\"");
		if (idIdx < 0) {
			return "unknown";
		}
		int start = idIdx + 6;
		int end = payload.indexOf("\"", start);
		return end > start ? payload.substring(start, end) : "unknown";
	}
	
	private static String getRequiredEnv(String key) {
		String value = System.getenv(key);
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalStateException("Verplichte environment variable ontbreekt: " + key);
		}
		return value;
	}
}
