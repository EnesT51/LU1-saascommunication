package com.api.RestAPI.infrastructure.HapiFhir.component;

import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Appointment;
import org.springframework.stereotype.Component;

import com.api.RestAPI.application.globalexceptions.InvalidFhirJsonException;
import com.api.RestAPI.infrastructure.HapiFhir.interfaces.IHapiFhirValidator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

@Component
public class HapiFhirValidator implements IHapiFhirValidator {

    private final FhirValidator fhirValidator;
    private final FhirContext fhirContext;

    public HapiFhirValidator(
            FhirValidator fhirValidator,
            FhirContext fhirContext
    ) {
        this.fhirValidator = fhirValidator;
        this.fhirContext = fhirContext;
    }

    @Override
    public void validate(String fhirJson) {
        if (fhirJson == null || fhirJson.isBlank()) {
                throw new InvalidFhirJsonException(
                        "FHIR payload cannot be null or empty"
            );
        }
        try{
            IParser parser = fhirContext.newJsonParser();
            Appointment resource = parser.parseResource(Appointment.class, fhirJson);
            ValidationResult result = fhirValidator.validateWithResult(resource);
            String validationMessage = result.getMessages()
                    .stream()
                    .filter(this::isValidationFailure)
                    .map(SingleValidationMessage::getMessage)
                    .collect(Collectors.joining(", "));

            if (!validationMessage.isBlank()) {
                throw new InvalidFhirJsonException("FHIR validation failed: " + validationMessage);
            }

            if (!result.isSuccessful()) {
                validationMessage = result.getMessages()
                        .stream()
                        .map(SingleValidationMessage::getMessage)
                        .collect(Collectors.joining(", "));

                if (validationMessage.isBlank()) {
                    validationMessage = "FHIR validation failed";
                }

                throw new InvalidFhirJsonException("FHIR validation failed: " + validationMessage);
            }
        } catch (InvalidFhirJsonException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidFhirJsonException("Error during FHIR validation: " + ex.getMessage());
        }
    }

    private boolean isValidationFailure(SingleValidationMessage message) {
        return message.getSeverity() == ResultSeverityEnum.ERROR
                || message.getSeverity() == ResultSeverityEnum.FATAL;
    }
}
