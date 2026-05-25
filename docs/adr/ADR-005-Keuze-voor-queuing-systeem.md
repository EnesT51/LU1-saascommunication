# ADR-005: RabbitMQ voor RestAPI → provider dispatch

## Status

Accepted
Datum: 21-05-2026

## Context

ADR-004 beschrijft waarom ActiveMQ gebruikt wordt voor het inkomende transport (OpenMRS → RestAPI). Voor het **uitgaande transport** (RestAPI → externe messaging providers) zijn de eisen anders:

- Per-provider isolatie: SwiftSend, LegacyLink, AsyncFlow en SecurePost moeten elk hun eigen queue krijgen zodat een trage of falende provider de anderen niet blokkeert
- Per-provider Dead Letter Queue (DLQ) voor permanent gefaalde berichten
- Retry-policy per queue configureerbaar
- Routering via exchanges (één publish, meerdere queues mogelijk in de toekomst)
- Goede observability via management API + Prometheus exporter

Voor deze use case is RabbitMQ beter geschikt dan ActiveMQ Classic.

## Decision

Voor het uitgaande transport (RestAPI → providers) wordt **RabbitMQ** gebruikt.

- Eén queue per provider: `swiftsend.queue`, `legacylink.queue`, `asyncflow.queue`, `securepost.queue`
- Eén DLQ per provider: `*.dlq`, gevoed door een `provider.dlx` dead-letter exchange
- Spring AMQP listeners voor consumption
- Management API op poort 15672 wordt door json-exporter ingelezen voor Prometheus

## Alternatives considered

### Dezelfde ActiveMQ broker hergebruiken

**Voordelen:** geen extra infrastructuur, één broker minder beheren

**Nadelen:**
- ActiveMQ Classic exchange/routing patroon is minder flexibel dan RabbitMQ's exchange-types
- DLQ-configuratie per queue is omslachtiger
- De OpenMRS ActiveMQ broker is bedoeld voor OpenMRS interne events; we willen onze provider-dispatch niet vermengen met OpenMRS events
- Loose coupling: als OpenMRS later overschakelt op een andere broker, hoeven wij niet mee te migreren

### Apache Kafka

**Voordelen:** zeer hoge throughput, persistent log

**Nadelen:**
- Overkill voor onze schaal (~maximaal duizenden berichten per dag per organisatie)
- Complexer in setup en operations
- Geen native DLQ-patroon (moet handmatig met topics)

## Consequences

### Voordelen

- Per-provider isolatie voorkomt cascading failures
- DLQ per provider maakt root-cause-analyse eenvoudig
- Management UI op poort 15672 voor handmatige inspectie
- json-exporter scrapet RabbitMQ Management API voor Prometheus metrics (zichtbaar in Grafana dashboard)

### Nadelen

- Twee message brokers (ActiveMQ + RabbitMQ) in de architectuur
  → expliciet gekozen, met duidelijke rolverdeling: ADR-004 dekt inkomend, dit ADR dekt uitgaand
- Extra Docker container + ~200 MB memory footprint
- Operationeel onderhoud van twee brokers in plaats van één

## Relatie met andere ADRs

- **ADR-004** beschrijft de inkomende kant (ActiveMQ)
- **ADR-006** beschrijft de monitoring van beide brokers
- **ADR-007** beschrijft hoe per-organisatie provider-routing op deze RabbitMQ-queues uitkomt
