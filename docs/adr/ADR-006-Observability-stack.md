# ADR-006: Observability stack — OpenTelemetry + Prometheus + Grafana + Loki

## Status

Accepted
Datum: 22-05-2026

## Context

NFR 9 vereist: *"De werking van de communicatiemodule dient volledig inzichtelijk te zijn via geschikte monitoringtooling, bijvoorbeeld op basis van OpenTelemetry. Er dient een real-time dashboard beschikbaar zijn waarop OpenMRS beheerders de status van berichten, prestaties (throughput) en eventuele foutmeldingen kunnen volgen om het systeem te bewaken."*

Concreet moet de stack:

- **Metrics** vastleggen: JVM, HTTP, Micrometer custom counters (`notifications_sent_total`, `notifications_failed_total`), queue diepte (ActiveMQ + RabbitMQ)
- **Logs** centraal verzamelen vanuit RestAPI containers
- **Traces** ondersteunen voor latency-analyse van de HTTP calls naar providers
- **Real-time dashboard** bieden waarop beheerders throughput, failures en queue-status zien
- **Open source** zijn zodat ziekenhuizen het zelf kunnen draaien zonder licentiekosten

## Decision

We gebruiken de **LGTM-stack** (Loki + Grafana + Tempo + Mimir) gebundeld in één container, met een **OpenTelemetry Collector** als pipeline:

```
RestAPI (OTel Java agent) ──┐
ActiveMQ (Jolokia)          │
RabbitMQ (Management API) ──┼─► OTel Collector ─► LGTM (Prometheus/Loki/Tempo) ─► Grafana
json-exporter ──────────────┘
```

### Onderdelen

| Component | Functie | Poort |
|---|---|---|
| OpenTelemetry Java agent | Auto-instrumentatie van RestAPI (JVM + HTTP + JDBC traces) | — |
| OpenTelemetry Collector | Receivers (OTLP, Prometheus scrape) + exporters | 4317 / 4318 |
| Prometheus (in LGTM) | Metrics storage + query | 9090 |
| Loki (in LGTM) | Logs storage + query | 3100 |
| Tempo (in LGTM) | Traces storage | 4418 |
| Grafana (in LGTM) | Dashboards | 3000 |
| json-exporter | Vertaalt RabbitMQ/ActiveMQ JSON API naar Prometheus format | 7979 |

### Dashboard

Eén Grafana dashboard `saascommunication-activemq` toont:

- ActiveMQ: berichten in queue, totaal verzonden/verwerkt, aantal consumers
- Berichtenflow (throughput): verstuurd vs verwerkt per minuut
- Foutrate: onverwerkte berichten (enqueued − dequeued)
- Notificaties per provider (sent + failed)
- RabbitMQ queue depth per provider + DLQ-tellers
- Loki paneel met ERROR logs uit alle services

### Custom metrics (NFR 9)

Toegevoegd aan de Spring Boot RestAPI via Micrometer:

- `notifications_sent_total{provider}` — counter per succesvolle dispatch
- `notifications_failed_total{provider, reason}` — counter per gefaalde dispatch

## Alternatives considered

### Seq

**Voordelen:** zeer mooi voor structured logs

**Nadelen:** alleen logs (geen metrics of traces), commerciële licentie voor productiegebruik, niet de standaard in cloud-native ziekenhuisomgevingen

### ELK Stack (Elasticsearch + Logstash + Kibana)

**Voordelen:** volwassen, breed gedragen

**Nadelen:** zwaar in resources (Elasticsearch heeft minimaal 2 GB RAM), drie verschillende producten configureren, geen native trace-support zonder APM-server, geen native Prometheus integratie

### Datadog / New Relic (SaaS)

**Voordelen:** out-of-the-box, mooie UI

**Nadelen:** vendor lock-in, kosten per host, patiëntgegevens zouden bij externe partij belanden (AVG-risico)

## Consequences

### Voordelen

- Eén container (`lgtm`) voor alle drie observability pillars: metrics, logs, traces
- OpenTelemetry is een open standaard — ziekenhuizen kunnen later naar elk OTLP-compatible backend overstappen zonder code-wijziging
- Volledig open source, geen licentiekosten
- Custom metrics zijn met Micrometer-annotaties triviaal toe te voegen
- Geen patiëntgegevens verlaten het ziekenhuis (in tegenstelling tot SaaS oplossingen)

### Nadelen

- LGTM container verbruikt ~1.5 GB RAM, niet geschikt voor laagvermogen-omgevingen
- Configuratie van OTel Collector (`otelcol-config.yaml`) is initieel niet triviaal
- Dashboard JSON wordt handmatig onderhouden (geen Terraform/Ansible)
