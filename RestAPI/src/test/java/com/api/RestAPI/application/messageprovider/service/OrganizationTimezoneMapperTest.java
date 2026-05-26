package com.api.RestAPI.application.messageprovider.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrganizationTimezoneMapper Tests - NFR 13")
class OrganizationTimezoneMapperTest {

    private OrganizationTimezoneMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrganizationTimezoneMapper();
        mapper.setTimezone(Map.of(
                "default",             "Europe/Amsterdam",
                "ziekenhuis-amsterdam","Europe/Amsterdam",
                "singapore-hospital",  "Asia/Singapore",
                "new-york-hospital",   "America/New_York"
        ));
    }

    @Test
    @DisplayName("NFR 13 - Nederlandse organisatie krijgt tijdzone Europe/Amsterdam")
    void resolveTimezoneReturnsDutchTimezone() {
        assertEquals(ZoneId.of("Europe/Amsterdam"), mapper.resolveTimezone("ziekenhuis-amsterdam"));
    }

    @Test
    @DisplayName("NFR 13 - Singaporese organisatie krijgt tijdzone Asia/Singapore")
    void resolveTimezoneReturnsSingaporeTimezone() {
        assertEquals(ZoneId.of("Asia/Singapore"), mapper.resolveTimezone("singapore-hospital"));
    }

    @Test
    @DisplayName("NFR 13 - New Yorkse organisatie krijgt tijdzone America/New_York")
    void resolveTimezoneReturnsNewYorkTimezone() {
        assertEquals(ZoneId.of("America/New_York"), mapper.resolveTimezone("new-york-hospital"));
    }

    @Test
    @DisplayName("NFR 13 - Onbekende organisatie valt terug op Europe/Amsterdam")
    void resolveTimezoneDefaultsToAmsterdamForUnknownOrganization() {
        assertEquals(ZoneId.of("Europe/Amsterdam"), mapper.resolveTimezone("onbekend-ziekenhuis"));
    }

    @Test
    @DisplayName("NFR 13 - Null organisatie valt terug op Europe/Amsterdam")
    void resolveTimezoneDefaultsToAmsterdamForNullOrganization() {
        assertEquals(ZoneId.of("Europe/Amsterdam"), mapper.resolveTimezone(null));
    }
}
