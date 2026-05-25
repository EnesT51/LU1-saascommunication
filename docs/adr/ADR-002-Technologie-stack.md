# ADR-002: Gekozen technologie stack voor de communicatiemodule

## Status

Accepted

## Context

De communicatiemodule moet:

- HL7/FHIR berichten verwerken met validatie
- Retry- en queue-mechanismen ondersteunen
- Multi-tenant SaaS ondersteunen
- Gevoelige data veilig behandelen
- Real-time monitoring en audit logging bieden

## Decision

### Backend — Spring Boot 4 (Java 21)

- Sterke ondersteuning voor REST en security
- Integratie met HAPI FHIR voor HL7/FHIR R4 verwerking
- Volwassen ecosysteem voor Spring AMQP (RabbitMQ) en Spring JMS (ActiveMQ)
- Teamervaring met Java

### Database — MariaDB

- Wordt al gebruikt binnen OpenMRS Docker omgeving
- Goede integratie met Spring (JPA/Hibernate)
- Geschikt voor opslag van afspraken en notificatie-meta-informatie

### Monitoring & logging — OpenTelemetry + Prometheus + Grafana (LGTM)

- OpenTelemetry collector ontvangt traces, metrics en logs via OTLP
- Prometheus scrapet `/actuator/prometheus`, ActiveMQ Jolokia en RabbitMQ Management API
- Grafana toont real-time dashboards voor throughput, queue diepte en error rates
- Loki ontvangt application logs voor centrale log-zoekfunctie
- Zie ADR-006 voor de volledige observability rationale

### Configuratie & secrets — `.env`

- Alle credentials (DB, providers, API keys) buiten broncode en compose.yaml gehouden
- Aanbevolen voor productie: vervangen door secrets manager (Vault / AWS Secrets Manager) — zie ADR-008

## Alternatives considered

- **Node.js / .NET backend** — minder teamervaring, minder HL7 tooling (HAPI FHIR is Java-native)
- **PostgreSQL** — geen voordeel t.o.v. MariaDB binnen deze context, MariaDB komt al mee met OpenMRS
- **Seq voor logging** — eerder overwogen maar afgevallen: Seq dekt alleen logs, terwijl we ook metrics en traces nodig hebben. De LGTM-stack (Loki+Grafana+Tempo+Mimir) dekt alle drie pillars in één deployment

## Consequences

### Voordelen

- Eén homogene Java-stack maakt onderhoud overzichtelijk
- LGTM-stack is open source en draait volledig lokaal in Docker
- HAPI FHIR neemt complexe FHIR-validatie uit handen

### Nadelen

- LGTM-stack vereist extra container resources (lgtm container ~1.5 GB RAM)
- Spring Boot 4 is relatief nieuw; documentatie van third-party libs loopt soms achter
