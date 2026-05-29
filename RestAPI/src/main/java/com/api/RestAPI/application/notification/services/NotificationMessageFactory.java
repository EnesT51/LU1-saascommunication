package com.api.RestAPI.application.notification.services;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.messageprovider.service.OrganizationTimezoneMapper;
import com.api.RestAPI.application.notification.interfaces.INotificationMessageFactory;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.notification.enums.NotificationType;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

@Service
public class NotificationMessageFactory implements INotificationMessageFactory {

    private static final Logger log = LoggerFactory.getLogger(NotificationMessageFactory.class);
    private static final DateTimeFormatter DATE_PATTERN =
            DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy 'at' HH:mm", Locale.ENGLISH);

    private final OrganizationTimezoneMapper timezoneMapper;

    public NotificationMessageFactory(OrganizationTimezoneMapper timezoneMapper) {
        this.timezoneMapper = timezoneMapper;
    }

    @Override
    public ProviderMessage create(AppointmentEntity appointment, ProviderType providerType,
            NotificationType notificationType, UUID messageId, UUID notificationId) {
        // NFR 13: gebruik tijdzone van de organisatie i.p.v. een vaste zone
        ZoneId zone = timezoneMapper.resolveTimezone(appointment.getOrganizationId());
        String dateTime = appointment.getStart() != null
                ? DATE_PATTERN.withZone(zone).format(appointment.getStart())
                : "unknown";

        String location = (appointment.getLocation() != null && !appointment.getLocation().isBlank())
                ? appointment.getLocation()
                : "see your appointment confirmation";

        StringBuilder content = new StringBuilder();

        if (notificationType == NotificationType.REMINDER_24H) {
            content.append("Reminder: you have an appointment tomorrow.\n\n");
        } else {
            content.append("Reminder: you have an appointment in 1 hour.\n\n");
        }

        content.append("Date & time  : ").append(dateTime).append("\n");
        content.append("Location     : ").append(location).append("\n");

        if (appointment.getComment() != null && !appointment.getComment().isBlank()) {
            content.append("Instructions : ").append(appointment.getComment()).append("\n");
        }

        content.append("\nAppointment ID: ").append(appointment.getAppointmentId());

        String smsText = content.toString();

        // NFR 5 (AVG/GDPR): geen patiëntgegevens of berichteninhoud in logs
        log.info("SMS gebouwd voor afspraak {} via provider {}",
                appointment.getAppointmentId(), providerType);

        return new ProviderMessage(providerType, appointment.getPatientPhoneNumber(),
                smsText, "Appointment reminder", messageId, notificationId);
    }
}
