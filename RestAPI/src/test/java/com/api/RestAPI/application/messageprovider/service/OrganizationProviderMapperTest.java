package com.api.RestAPI.application.messageprovider.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.domain.message.enums.ProviderType;

@DisplayName("OrganizationProviderMapper Tests - NFR 1 & NFR 3")
class OrganizationProviderMapperTest {

    private OrganizationProviderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrganizationProviderMapper();
        mapper.setMapping(Map.of(
                "ziekenhuis-amsterdam", "SWIFTSEND",
                "amsterdam-amc",       "ASYNCFLOW",
                "rotterdam-erasmus",   "LEGACYLINK",
                "utrecht-umcu",        "SECUREPOST",
                "default",             "SWIFTSEND"
        ));
        mapper.setFallback("LEGACYLINK");
    }

    // ── NFR 3: alle 4 providers worden ondersteund ──────────────────────────

    @Test
    @DisplayName("NFR 3 - SwiftSend provider wordt ondersteund")
    void resolvePrimaryReturnsSwiftSend() {
        assertEquals(ProviderType.SWIFTSEND, mapper.resolvePrimary("ziekenhuis-amsterdam"));
    }

    @Test
    @DisplayName("NFR 3 - AsyncFlow provider wordt ondersteund")
    void resolvePrimaryReturnsAsyncFlow() {
        assertEquals(ProviderType.ASYNCFLOW, mapper.resolvePrimary("amsterdam-amc"));
    }

    @Test
    @DisplayName("NFR 3 - LegacyLink provider wordt ondersteund")
    void resolvePrimaryReturnsLegacyLink() {
        assertEquals(ProviderType.LEGACYLINK, mapper.resolvePrimary("rotterdam-erasmus"));
    }

    @Test
    @DisplayName("NFR 3 - SecurePost provider wordt ondersteund")
    void resolvePrimaryReturnsSecurePost() {
        assertEquals(ProviderType.SECUREPOST, mapper.resolvePrimary("utrecht-umcu"));
    }

    // ── NFR 1: elk ziekenhuis kan zijn eigen provider kiezen ─────────────────

    @Test
    @DisplayName("NFR 1 - Onbekend ziekenhuis valt terug op default provider")
    void resolvePrimaryFallsBackToDefaultForUnknownOrganization() {
        assertEquals(ProviderType.SWIFTSEND, mapper.resolvePrimary("onbekend-ziekenhuis"));
    }

    @Test
    @DisplayName("NFR 1 - Fallback-lijst bevat niet de primaire provider")
    void resolveFallbacksExcludesPrimaryProvider() {
        List<ProviderType> fallbacks = mapper.resolveFallbacks(ProviderType.SWIFTSEND);

        assertFalse(fallbacks.contains(ProviderType.SWIFTSEND));
        assertTrue(fallbacks.contains(ProviderType.LEGACYLINK));
        assertTrue(fallbacks.contains(ProviderType.ASYNCFLOW));
        assertTrue(fallbacks.contains(ProviderType.SECUREPOST));
    }

    @Test
    @DisplayName("NFR 1 - Geconfigureerde fallback staat als eerste in de lijst")
    void resolveFallbacksPutsConfiguredFallbackFirst() {
        List<ProviderType> fallbacks = mapper.resolveFallbacks(ProviderType.SWIFTSEND);

        assertEquals(ProviderType.LEGACYLINK, fallbacks.get(0));
    }
}
