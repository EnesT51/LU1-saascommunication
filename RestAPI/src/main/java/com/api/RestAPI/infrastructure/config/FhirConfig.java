package com.api.RestAPI.infrastructure.config;

import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;

@Configuration
public class FhirConfig {
    
    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }
    
    @Bean
    public FhirValidator fhirValidator(FhirContext fhirContext) {
        
        FhirValidator validator = fhirContext.newValidator();

        DefaultProfileValidationSupport validationSupport =
                new DefaultProfileValidationSupport(fhirContext);

        FhirInstanceValidator instanceValidator =
                new FhirInstanceValidator(validationSupport);

        instanceValidator.setAnyExtensionsAllowed(true);
        instanceValidator.setErrorForUnknownProfiles(true);
        instanceValidator.setNoTerminologyChecks(true);
        
        validator.registerValidatorModule(instanceValidator);

        return validator;
    }
}
