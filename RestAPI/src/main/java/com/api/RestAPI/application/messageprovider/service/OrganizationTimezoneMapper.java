package com.api.RestAPI.application.messageprovider.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapt een organizationId naar de lokale tijdzone van die organisatie.
 *
 * Configuratie via application.properties:
 *   organization.timezone.default=Europe/Amsterdam
 *   organization.timezone.amsterdam-amc=Europe/Amsterdam
 *   organization.timezone.singapore-hospital=Asia/Singapore
 *
 * Nieuwe organisatie toevoegen: voeg één regel toe in application.properties.
 * Geldige tijdzone-IDs: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
 */
@Component
@ConfigurationProperties(prefix = "organization")
public class OrganizationTimezoneMapper {

    private static final Logger log = LoggerFactory.getLogger(OrganizationTimezoneMapper.class);
    private static final String DEFAULT_KEY = "default";
    private static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("Europe/Amsterdam");

    private Map<String, String> timezone = new HashMap<>();

    /**
     * Geeft de tijdzone voor de gegeven organizationId.
     * Valt terug op "default", dan op Europe/Amsterdam.
     */
    public ZoneId resolveTimezone(String organizationId) {
        if (organizationId != null && !organizationId.isBlank()) {
            ZoneId resolved = parseZoneId(timezone.get(organizationId.toLowerCase()));
            if (resolved != null) return resolved;
        }
        ZoneId defaultZone = parseZoneId(timezone.get(DEFAULT_KEY));
        return defaultZone != null ? defaultZone : DEFAULT_TIMEZONE;
    }

    private ZoneId parseZoneId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return ZoneId.of(value);
        } catch (Exception e) {
            log.warn("Ongeldige tijdzone '{}', gebruik default", value);
            return null;
        }
    }

    public Map<String, String> getTimezone() { return timezone; }
    public void setTimezone(Map<String, String> timezone) { this.timezone = timezone; }
}
