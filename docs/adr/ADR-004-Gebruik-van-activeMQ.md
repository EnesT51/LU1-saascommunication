# ADR-004: Gebruik van ActiveMQ als message broker

## Status

Accepted

## Context

De communicatiemodule moet asynchroon berichten verwerken zonder afhankelijk te zijn van OpenMRS of externe messaging providers.

Eisen:

- Berichten mogen niet verloren gaan
- Retry-mechanisme bij fouten
- Lage coupling tussen systemen
- Multi-tenant ondersteuning
- Beveiligde communicatie
- Monitoring van message flows

Opties:

1. Directe communicatie zonder broker
2. RabbitMQ
3. ActiveMQ (reeds aanwezig in OpenMRS)

## Decision

Er wordt gebruik gemaakt van de bestaande ActiveMQ broker binnen OpenMRS.

## Alternatives considered

### Directe communicatie

- Sterke afhankelijkheid
- Geen retry mogelijkheden
- Kans op berichtverlies

### RabbitMQ

- Extra infrastructuur nodig
- Overbodig omdat ActiveMQ al aanwezig is

## Consequences

### Voordelen

- Geen extra infrastructuur
- Betrouwbare aflevering van berichten
- Ondersteunt asynchrone verwerking
- Eenvoudig te monitoren
- Past bij bestaande OpenMRS architectuur

### Nadelen

- Afhankelijkheid van OpenMRS broker
- Extra configuratie nodig voor consumers
