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

public class ActiveMqPublisher implements AppointmentPublisher {
	
	private static final String BROKER_URL = getRequiredEnv("ACTIVEMQ_BROKER_URL");
	
	private static final String QUEUE_NAME = getRequiredEnv("ACTIVEMQ_QUEUE_NAME");
	
	private static final String WEB_API_URL = getRequiredEnv("ACTIVEMQ_WEB_API_URL");
	
	private static final String WEB_API_USERNAME = getRequiredEnv("ACTIVEMQ_USERNAME");
	
	private static final String WEB_API_PASSWORD = getRequiredEnv("ACTIVEMQ_PASSWORD");
	
	public void publishAppointment(String payload) throws Exception {
		try {
			publishViaJms(payload);
		}
		catch (Exception jmsException) {
			publishViaWebApi(payload);
		}
	}
	
	private void publishViaJms(String payload) throws Exception {
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
	
	private static boolean isNullOrEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}
}
