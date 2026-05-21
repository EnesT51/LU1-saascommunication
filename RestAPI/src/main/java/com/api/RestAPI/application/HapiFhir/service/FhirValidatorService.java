package com.api.RestAPI.application.HapiFhir.service;

import com.api.RestAPI.application.HapiFhir.interfaces.IFhirValidatorService;
import com.api.RestAPI.infrastructure.HapiFhir.interfaces.IHapiFhirValidator;

public class FhirValidatorService implements IFhirValidatorService {

    private final IHapiFhirValidator officialFhirValidator;

    public FhirValidatorService(IHapiFhirValidator officialFhirValidator) {
        this.officialFhirValidator = officialFhirValidator;
    }

    @Override
    public void validate(String fhirJson) {

        officialFhirValidator.validate(fhirJson);
    }
    
}
