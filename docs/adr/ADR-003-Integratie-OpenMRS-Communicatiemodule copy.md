# ADR-003: Event-driven integratie via ActiveMQ tussen OpenMRS en communicatiemodule

## Status

Accepted

## Context

De communicatiemodule moet afspraakdata ontvangen vanuit OpenMRS.

Eisen:

- HL7/FHIR compliant
- Real-time verwerking
- Robuust bij downtime
- Schaalbaar voor meerdere ziekenhuizen
- Veilige overdracht van data

OpenMRS beschikt over een ingebouwde ActiveMQ message broker via de Event module.

Mogelijke opties:

1. Directe database koppeling
2. Polling via REST API
3. Event-driven via message broker (ActiveMQ)

## Decision

OpenMRS publiceert afspraak-events naar ActiveMQ.  
De communicatiemodule luistert als consumer op deze queue.

Bij ontvangst van een bericht voert de communicatiemodule een:
POST /appointment/save
uit om de afspraak lokaal op te slaan.

## Alternatives considered

### Directe database koppeling

- Onveilig
- Niet HL7 compliant
- Breekbaar bij updates

### Polling via REST API

- Niet real-time
- Inefficiënt
- Hoge API belasting

## Consequences

### Voordelen

- Real-time verwerking
- Losse koppeling tussen systemen
- Betrouwbaar door queue-mechanisme
- Past binnen event-driven architectuur

### Nadelen

- Complexere implementatie
- Afhankelijk van message broker infrastructuur
