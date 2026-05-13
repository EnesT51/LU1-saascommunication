package org.openmrs.module.saascommunication.messaging;

import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class ActiveMqPublisher {
	
	private static final String BROKER_URL = getEnvOrDefault("ACTIVEMQ_BROKER_URL", "tcp://activemq-classic-omrs:61616");
	
	private static final String QUEUE_NAME = "openmrs.appointments";
	
	private static final String WEB_API_URL = getEnvOrDefault("ACTIVEMQ_WEB_API_URL",
	    "http://activemq-classic-omrs:8161/api/message/" + QUEUE_NAME + "?type=queue");
	
	private static final String WEB_API_USERNAME = getRequiredEnv("ACTIVEMQ_USERNAME");
	
	private static final String WEB_API_PASSWORD = getRequiredEnv("ACTIVEMQ_PASSWORD");
	
	public void publishTestAppointmentEvent() throws Exception {
		try {
			publishViaJms(createTestAppointmentPayloads());
		}
		catch (Exception jmsException) {
			publishViaWebApi(createTestAppointmentPayloads());
		}
	}
	
	private void publishViaJms(List<String> payloads) throws Exception {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
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
			
			Destination destination = session.createQueue(QUEUE_NAME);
			producer = session.createProducer(destination);
			
			for (String payload : payloads) {
				TextMessage message = session.createTextMessage(payload);
				producer.send(message);
			}
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
	
	private void publishViaWebApi(List<String> payloads) throws Exception {
		for (String payload : payloads) {
			HttpURLConnection connection = null;
			
			try {
				URL url = new URL(WEB_API_URL);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/fhir+json");
				connection.setRequestProperty("Authorization", "Basic " + getBasicAuthToken());
				
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
					throw new IllegalStateException("ActiveMQ web API returned status " + responseCode);
				}
			}
			finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
	}
	
	private String getBasicAuthToken() {
		String credentials = WEB_API_USERNAME + ":" + WEB_API_PASSWORD;
		return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
	}
	
	private static String getRequiredEnv(String key) {
		String value = System.getenv(key);
		if (isNullOrEmpty(value)) {
			throw new IllegalStateException("Missing required environment variable: " + key);
		}
		return value;
	}
	
	private static String getEnvOrDefault(String key, String defaultValue) {
		String value = System.getenv(key);
		return isNullOrEmpty(value) ? defaultValue : value;
	}
	
	private static boolean isNullOrEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}
	
	private List<String> createTestAppointmentPayloads() {
		return Arrays.asList(createAppointmentOne(), createAppointmentTwo());
	}
	
	private String createAppointmentOne() {
		return "{"
		        + "\"resourceType\":\"Appointment\","
		        + "\"id\":\"appt-123\","
		        + "\"status\":\"booked\","
		        + "\"serviceCategory\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/service-category\",\"code\":\"17\",\"display\":\"General Practice\"}]}],"
		        + "\"appointmentType\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0276\",\"code\":\"FOLLOWUP\",\"display\":\"Follow-up\"}]},"
		        + "\"reasonCode\":[{\"text\":\"Controle consult\"}],"
		        + "\"description\":\"Controle afspraak bij polikliniek A\","
		        + "\"start\":\"2026-05-09T10:00:00+02:00\","
		        + "\"end\":\"2026-05-09T10:30:00+02:00\","
		        + "\"created\":\"2026-05-08T10:00:00Z\","
		        + "\"comment\":\"Neem medicatie mee\","
		        + "\"participant\":["
		        + "{\"actor\":{\"reference\":\"Patient/patient-456\",\"display\":\"Test Patient 1\"},\"status\":\"accepted\"},"
		        + "{\"actor\":{\"reference\":\"Location/location-12\",\"display\":\"Polikliniek A, kamer 12\"},\"status\":\"accepted\"}"
		        + "]" + "}";
	}
	
	private String createAppointmentTwo() {
		return "{"
		        + "\"resourceType\":\"Appointment\","
		        + "\"id\":\"appt-789\","
		        + "\"status\":\"booked\","
		        + "\"serviceCategory\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/service-category\",\"code\":\"26\",\"display\":\"Specialist Surgical\"}]}],"
		        + "\"appointmentType\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0276\",\"code\":\"ROUTINE\",\"display\":\"Routine\"}]},"
		        + "\"reasonCode\":[{\"text\":\"Intake gesprek\"}],"
		        + "\"description\":\"Nieuwe intake afspraak bij polikliniek B\","
		        + "\"start\":\"2026-05-10T14:00:00+02:00\","
		        + "\"end\":\"2026-05-10T14:45:00+02:00\","
		        + "\"created\":\"2026-05-08T11:00:00Z\","
		        + "\"comment\":\"Vergeet identiteitsbewijs niet\","
		        + "\"participant\":["
		        + "{\"actor\":{\"reference\":\"Patient/patient-999\",\"display\":\"Test Patient 2\"},\"status\":\"accepted\"},"
		        + "{\"actor\":{\"reference\":\"Location/location-22\",\"display\":\"Polikliniek B, kamer 4\"},\"status\":\"accepted\"}"
		        + "]" + "}";
	}
}
