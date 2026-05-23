package com.api.RestAPI.application.messageprovider.service;

import com.api.RestAPI.domain.message.enums.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapt een organizationId naar een primaire en fallback messaging provider.
 *
 * Configuratie via application.properties:
 *   provider.mapping.amsterdam-amc=SWIFTSEND
 *   provider.mapping.default=SWIFTSEND
 *   provider.fallback=LEGACYLINK
 */
@Component
@ConfigurationProperties(prefix = "provider")
public class OrganizationProviderMapper {

    private static final Logger log = LoggerFactory.getLogger(OrganizationProviderMapper.class);
    private static final String DEFAULT_KEY = "default";
    private static final ProviderType DEFAULT_PROVIDER = ProviderType.SWIFTSEND;

    private Map<String, String> mapping = new HashMap<>();
    private String fallback = "LEGACYLINK";

    /**
     * Geeft de primaire provider voor de gegeven organizationId.
     * Valt terug op "default" mapping, dan op SWIFTSEND.
     */
    public ProviderType resolvePrimary(String organizationId) {
        if (organizationId != null && !organizationId.isBlank()) {
            ProviderType resolved = parseProviderType(mapping.get(organizationId.toLowerCase()), null);
            if (resolved != null) return resolved;
        }
        return parseProviderType(mapping.get(DEFAULT_KEY), DEFAULT_PROVIDER);
    }

    /**
     * Geeft de fallback provider — altijd een andere dan de primaire.
     * Kiest de eerste beschikbare ProviderType die niet gelijk is aan primary.
     */
    public ProviderType resolveFallback(ProviderType primary) {
        ProviderType configured = parseProviderType(fallback, null);

        if (configured != null && configured != primary) {
            return configured;
        }

        // Kies de eerste ProviderType die niet gelijk is aan primary
        return Arrays.stream(ProviderType.values())
                .filter(p -> p != primary)
                .findFirst()
                .orElse(primary);
    }

    /**
     * Parst een provider string naar ProviderType. Geeft defaultValue terug bij null of ongeldig.
     */
    private ProviderType parseProviderType(String value, ProviderType defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return ProviderType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Onbekende provider waarde '{}', gebruik default", value);
            return defaultValue;
        }
    }

    public Map<String, String> getMapping() { return mapping; }
    public void setMapping(Map<String, String> mapping) { this.mapping = mapping; }
    public String getFallback() { return fallback; }
    public void setFallback(String fallback) { this.fallback = fallback; }
}
