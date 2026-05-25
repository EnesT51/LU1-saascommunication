# ADR-001: Communicatiemodule als zelfstandige SaaS service

## Status

Accepted

## Context

OpenMRS moet worden uitgebreid met een communicatiemodule die afspraaknotificaties verstuurt via externe messaging providers (SwiftSend, LegacyLink, AsyncFlow, SecurePost).

De module moet:

- werken als SaaS voor meerdere ziekenhuizen (multi-tenant)
- voldoen aan HL7/FHIR standaarden
- schaalbaar en uitbreidbaar zijn
- veilig omgaan met patiëntdata
- losgekoppeld zijn van OpenMRS versies (2.7+)

Er zijn twee opties overwogen:

1. Ingebouwde OpenMRS module (plugin)
2. Zelfstandige externe SaaS service

## Decision

De communicatiemodule wordt opgezet als een zelfstandige SaaS applicatie, los van OpenMRS. Communicatie verloopt via API’s op basis van FHIR/REST.

## Alternatives considered

### Ingebouwde OpenMRS module

- Sterk afhankelijk van OpenMRS versies
- Niet geschikt voor multi-tenant SaaS
- Moeilijk uitbreidbaar met externe messaging providers
- Kwetsbaar bij updates van OpenMRS

## Consequences

### Voordelen

- Schaalbaar voor meerdere OpenMRS-instanties
- Losse koppeling met OpenMRS
- Geschikt voor abonnementenmodel
- Centrale beveiliging, logging en monitoring
- Eenvoudig uitbreidbaar met nieuwe providers

### Nadelen

- Complexere architectuur
- Extra infrastructuur nodig
- Netwerkafhankelijkheid
- Extra aandacht nodig voor security (OAuth2, TLS)
