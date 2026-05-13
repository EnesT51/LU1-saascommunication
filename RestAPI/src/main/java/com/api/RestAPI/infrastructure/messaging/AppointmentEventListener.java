package com.api.RestAPI.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;
// import com.api.RestAPI.application.messaging.AppointmentEventStore;

// @Component
// public class AppointmentEventListener {

//     private static final Logger log = LoggerFactory.getLogger(AppointmentEventListener.class);

//     private final IAppointmentEventProcessor appointmentEventProcessor;
//     private final AppointmentEventStore eventStore;

//     public AppointmentEventListener(IAppointmentEventProcessor appointmentEventProcessor, AppointmentEventStore eventStore) {
//         this.appointmentEventProcessor = appointmentEventProcessor;
//         this.eventStore = eventStore;
//     }

//     @JmsListener(destination = "${app.queue.name}")
//     public void receive(String payload) {
//         try {
//             appointmentEventProcessor.processAppointmentEvent(payload);
//             eventStore.store(payload);
//             log.info("Received and processed appointment event from ActiveMQ");
//         } catch (Exception e) {
//             log.error("Failed to process appointment event from ActiveMQ: {}", e.getMessage(), e);
//         }
//     }
// }
