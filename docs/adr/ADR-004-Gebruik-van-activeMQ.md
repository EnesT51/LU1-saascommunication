# ADR-004: ActiveMQ voor OpenMRS → RestAPI transport

## Status

Accepted

## Context

We hebben twee verschillende messaging-behoeften in de architectuur:

1. **Inkomend:** OpenMRS publiceert afspraak-events naar onze module
2. **Uitgaand:** De module dispatcht notificaties naar externe providers (SwiftSend / LegacyLink / AsyncFlow / SecurePost)

Voor de inkomende kant geldt: OpenMRS heeft via de Event-module al een ingebouwde ActiveMQ broker. Elke afspraak die in OpenMRS wordt opgeslagen of geannuleerd kan via een AfterReturningAdvice (AOP) als JMS message naar deze broker worden gepubliceerd.

Eisen voor inkomend transport:

- HL7/FHIR R4 berichten doorgeven
- Real-time verwerking, geen polling
- Robuust bij downtime van onze module
- Veilige overdracht (TLS)

## Decision

Voor de inkomende kant (OpenMRS → RestAPI) wordt **ActiveMQ Classic** gebruikt:

- OpenMRS publiceert via AOP advice op de `appointments` queue
- De RestAPI (`AppointmentEventListener`) consumeert deze queue
- TLS afgedwongen via SSL truststore (`saasdev2026`)

Voor de uitgaande kant (RestAPI → providers) wordt **RabbitMQ** gebruikt — zie **ADR-005** voor die keuze en de motivatie waarom we voor uitgaand niet dezelfde broker gebruiken.

## Alternatives considered voor inkomend transport

### Directe HTTP-call vanuit OpenMRS

- Sterke afhankelijkheid; onze module moet altijd up zijn
- Geen ingebouwde retry of buffer
- Verlies van events bij netwerkonderbreking

### Polling via REST API

- Niet real-time
- Hoge API belasting op OpenMRS

### Database trigger / shared DB

- Sterk gekoppeld aan OpenMRS schema
- Niet HL7 compliant
- Breekbaar bij OpenMRS updates

## Consequences

### Voordelen

- Geen extra broker installatie voor inkomend transport (ActiveMQ is al onderdeel van OpenMRS)
- Buffering bij downtime van de RestAPI
- Losse koppeling tussen OpenMRS en onze module
- Past binnen event-driven architectuur

### Nadelen

- Afhankelijkheid van OpenMRS ActiveMQ Classic versie
- Twee verschillende brokers (ActiveMQ + RabbitMQ) in de stack
  → bewuste keuze: zie ADR-005 voor de rationale waarom uitgaand een andere broker krijgt
