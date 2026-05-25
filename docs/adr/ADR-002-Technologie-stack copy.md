# ADR-002: Gekozen technologie stack voor de communicatiemodule

## Status

Accepted

## Context

De communicatiemodule moet:

- HL7/FHIR berichten verwerken met ACK en validatie
- Retry- en queue-mechanismen ondersteunen
- Multi-tenant SaaS ondersteunen
- Gevoelige data versleuteld opslaan (AES-256)
- Real-time monitoring en audit logging bieden

## Decision

### Backend — Spring Boot (Java)

- Sterke ondersteuning voor REST en security (OAuth2, TLS)
- Integratie met HAPI FHIR voor HL7/FHIR verwerking
- Betrouwbaar binnen zorgsystemen
- Teamervaring met Java

### Database — MariaDB

- Wordt al gebruikt binnen OpenMRS Docker omgeving
- Goede integratie met Spring (JPA/Hibernate)
- Ondersteuning voor JSON/FHIR data
- Opslag van versleutelde credentials en meta-informatie

### Monitoring & logging — Seq

- Real-time inzicht in events en fouten
- Geschikt voor asynchrone processen
- Eenvoudige integratie met Spring logging

## Alternatives considered

- Node.js / .NET backend (minder teamervaring, minder HL7 tooling)
- PostgreSQL (geen voordeel t.o.v. MariaDB binnen deze context)
