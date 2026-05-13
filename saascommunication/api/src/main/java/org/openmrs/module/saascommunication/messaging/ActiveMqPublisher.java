package org.openmrs.module.saascommunication.messaging;

import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
			publishViaJms();
		}
		catch (Exception jmsException) {
			publishViaWebApi();
		}
	}
	
	private void publishViaJms() throws Exception {
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
			
			TextMessage message = session.createTextMessage(createTestAppointmentJson());
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
	
	private void publishViaWebApi() throws Exception {
		HttpURLConnection connection = null;
		
		try {
			URL url = new URL(WEB_API_URL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Basic " + getBasicAuthToken());
			
			byte[] payload = createTestAppointmentJson().getBytes(StandardCharsets.UTF_8);
			connection.setFixedLengthStreamingMode(payload.length);
			
			OutputStream outputStream = connection.getOutputStream();
			try {
				outputStream.write(payload);
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
	
	private String getBasicAuthToken() {
		String credentials = WEB_API_USERNAME + ":" + WEB_API_PASSWORD;
		return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
	}

	private static String getRequiredEnv(String key) {
		String value = System.getenv(key);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing required environment variable: " + key);
		}
		return value;
	}

	private static String getEnvOrDefault(String key, String defaultValue) {
		String value = System.getenv(key);
		return (value == null || value.isBlank()) ? defaultValue : value;
	}
	
	private String createTestAppointmentJson() {
		return "{" + "\"eventId\":\"test-from-openmrs-module\"," + "\"eventType\":\"AppointmentScheduled\","
		        + "\"timestamp\":\"2026-05-08T10:00:00Z\"," + "\"payload\":{" + "\"appointmentId\":\"appt-123\","
		        + "\"patientId\":\"patient-456\"," + "\"start\":\"2026-05-09T10:00:00+02:00\","
		        + "\"location\":\"Polikliniek A, kamer 12\"," + "\"instructions\":\"Neem medicatie mee\"" + "}" + "}";
	}
}
