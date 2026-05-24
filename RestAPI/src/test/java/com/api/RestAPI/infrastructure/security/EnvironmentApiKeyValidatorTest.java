package com.api.RestAPI.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EnvironmentApiKeyValidator Tests")
class EnvironmentApiKeyValidatorTest {

    private final EnvironmentApiKeyValidator validator = new EnvironmentApiKeyValidator("secret");

    @Test
    @DisplayName("Should accept matching API key")
    void isValidAcceptsMatchingKey() {
        assertTrue(validator.isValid("secret"));
    }

    @Test
    @DisplayName("Should reject different API key")
    void isValidRejectsDifferentKey() {
        assertFalse(validator.isValid("wrong"));
    }

    @Test
    @DisplayName("Should reject null API key")
    void isValidRejectsNullKey() {
        assertFalse(validator.isValid(null));
    }
}
