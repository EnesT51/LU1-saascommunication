package com.api.RestAPI.application.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.domain.security.ApiKeyValidator;

@DisplayName("ApiKeyValidationService Tests")
class ApiKeyValidationServiceTest {

    @Test
    @DisplayName("Should reject null API key without calling validator")
    void isValidApiKeyRejectsNull() {
        ApiKeyValidator validator = mock(ApiKeyValidator.class);
        ApiKeyValidationService service = new ApiKeyValidationService(validator);

        assertFalse(service.isValidApiKey(null));
        verify(validator, never()).isValid(null);
    }

    @Test
    @DisplayName("Should reject empty API key without calling validator")
    void isValidApiKeyRejectsEmpty() {
        ApiKeyValidator validator = mock(ApiKeyValidator.class);
        ApiKeyValidationService service = new ApiKeyValidationService(validator);

        assertFalse(service.isValidApiKey(""));
        verify(validator, never()).isValid("");
    }

    @Test
    @DisplayName("Should delegate non-empty API key to validator")
    void isValidApiKeyDelegatesToValidator() {
        ApiKeyValidator validator = mock(ApiKeyValidator.class);
        ApiKeyValidationService service = new ApiKeyValidationService(validator);
        when(validator.isValid("secret")).thenReturn(true);

        assertTrue(service.isValidApiKey("secret"));
        verify(validator).isValid("secret");
    }
}
