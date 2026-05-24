package com.api.RestAPI.presentation.notification.controller;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.presentation.notification.response.NotificationSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final INotificationRepository notificationRepository;

    public NotificationController(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Geeft een overzicht van verstuurde notificaties per organisatie en messaging provider.
     * Bruikbaar voor het controleren van facturen van messaging providers.
     *
     * GET /api/notifications/summary
     * Header: X-API-KEY: <api-key>
     *
     * Voorbeeld response:
     * [
     *   { "organizationId": "amsterdam-amc", "provider": "ASYNCFLOW", "sent": 42, "failed": 3, "cancelled": 1, "total": 46 },
     *   { "organizationId": "rotterdam-erasmus", "provider": "LEGACYLINK", "sent": 17, "failed": 0, "cancelled": 2, "total": 19 }
     * ]
     */
    @GetMapping("/summary")
    public List<NotificationSummaryResponse> getSummary() {
        return notificationRepository.getSummary();
    }
}
