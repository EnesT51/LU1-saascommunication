# SaaS Communicatiemodule voor OpenMRS

Een SaaS-platform dat namens OpenMRS-organisaties wereldwijd notificaties verstuurt
naar patiënten via één van vier externe messaging providers
(**SwiftSend**, **LegacyLink**, **AsyncFlow**, **SecurePost**).

De module ontvangt afspraken vanuit OpenMRS (via FHIR R4), plant 24h- en 1h-herinneringen
en levert die af bij de juiste provider per ziekenhuis. Bij downtime wordt automatisch
naar een fallback-provider geschakeld, mislukte berichten gaan naar een Dead Letter Queue
voor handmatige inspectie.

---

## Architectuur in één blik

```
OpenMRS ──AOP advice──► ActiveMQ ──► RestAPI ──► MariaDB
                                        │
                                        └──► RabbitMQ ──► Provider (SwiftSend / LegacyLink / AsyncFlow / SecurePost)
                                                     └──► DLQ
Monitoring: OpenTelemetry + Prometheus + Grafana
```

- **OpenMRS module** (`saascommunication/`) – AOP advice intercepteert nieuwe of gewijzigde afspraken, valideert tegen HL7 FHIR R4 en publiceert naar ActiveMQ.
- **RestAPI** (`RestAPI/`) – Spring Boot service die ActiveMQ consumeert, notificaties plant en aflevert via RabbitMQ.
- **RabbitMQ** – één queue per provider met een Dead Letter Exchange voor permanent mislukte berichten.
- **MariaDB** – persistente opslag voor afspraken en notificatie-meta-info.
- **Grafana / Prometheus / OpenTelemetry** – real-time monitoring van throughput, success-rate en foutmeldingen.

Zie `docs/` voor de C4 diagrammen (levels 1 t/m 3), ADR-logboek en het realisatielogboek.

---

## Vereisten

| Tool | Versie |
|---|---|
| Docker Desktop | 4.x of nieuwer |
| Docker Compose | v2 (komt mee met Docker Desktop) |
| Java | 17 (alleen nodig als je de RestAPI lokaal buiten Docker draait) |
| Maven | 3.9+ (idem) |

---

## Stap 1 — Configureren

1. Kopieer het voorbeeld-`.env`-bestand:

   ```bash
   cp .env.example .env
   ```

   > **Belangrijk (NFR 5):** alle credentials en provider API-keys staan in `.env` en mogen niet in code of `application.properties` worden gezet.

2. Vul de provider-credentials in (deze ontvang je van je student-account voor de
   fake providers). Het `.env` bestand bevat onder andere:

   ```env
   # API key voor de RestAPI (X-API-KEY header)
   API_KEY=jouw-api-key-hier

   # Database
   DB_HOST=db
   DB_PORT=3306
   DB_NAME=saascomm
   DB_USER=root
   DB_PASSWORD=verander-mij

   # ActiveMQ (OpenMRS → RestAPI)
   ACTIVEMQ_BROKER_URL=tcp://activemq:61616

   # RabbitMQ (RestAPI → Providers)
   RABBITMQ_HOST=rabbitmq
   RABBITMQ_PORT=5672

   # Provider credentials
   SWIFTSEND_URL=...
   SWIFTSEND_API_KEY=...
   LEGACYLINK_URL=...
   LEGACYLINK_AUTHORIZATION=...
   ASYNCFLOW_URL=...
   ASYNCFLOW_API_KEY=...
   SECUREPOST_AUTH_URL=...
   SECUREPOST_MESSAGE_URL=...
   SECUREPOST_CLIENT_ID=...
   SECUREPOST_CLIENT_SECRET=...
   STUDENT_GROUP=jouw-studentgroep
   ```

---

## Stap 2 — Opstarten

```bash
docker-compose up -d --build
```

Wacht ongeveer 1–2 minuten tot alle services healthy zijn (vooral OpenMRS en de
RestAPI hebben even nodig). Controleer met:

```bash
docker-compose ps
```

### Services & poorten

| Service | URL | Doel |
|---|---|---|
| OpenMRS frontend | http://localhost:8080/openmrs/spa | Inloggen, afspraken aanmaken |
| RestAPI | http://localhost:8081 | REST endpoints voor notificaties |
| RabbitMQ Management | http://localhost:15672 (`guest` / `guest`) | Queues / DLQ inspecteren |
| ActiveMQ Console | http://localhost:8161 | OpenMRS → RestAPI berichtenverkeer |
| Grafana | http://localhost:3000 (`admin` / `admin`) | Live dashboard met throughput + foutmeldingen |
| MariaDB | localhost:3307 | Persistente opslag |

---

## Stap 3 — Voorbeeld request

De RestAPI is beveiligd met een API-key. Stuur deze mee in elke request via de
`X-API-KEY` header.

### Een afspraak handmatig vastleggen (FHIR R4 payload)

```bash
curl -X POST http://localhost:8081/api/appointments \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: jouw-api-key-hier" \
  -d '{
    "resourceType": "Appointment",
    "id": "afspraak-12345",
    "status": "booked",
    "start": "2026-06-01T10:00:00Z",
    "end":   "2026-06-01T10:30:00Z",
    "comment": "Kom 10 minuten van tevoren, draag makkelijke kleding",
    "participant": [
      {
        "actor": {
          "display": "+31612345678"
        },
        "status": "accepted"
      }
    ],
    "extension": [
      {
        "url": "http://example.org/fhir/StructureDefinition/organization-id",
        "valueString": "ziekenhuis-amsterdam"
      },
      {
        "url": "http://example.org/fhir/StructureDefinition/location",
        "valueString": "Polikliniek interne geneeskunde, kamer 4"
      }
    ]
  }'
```

In de praktijk komt deze payload automatisch vanuit OpenMRS via ActiveMQ — bovenstaande
`curl` is alleen voor handmatig testen.

### Overzicht van verstuurde notificaties opvragen (voor facturatie)

```bash
curl http://localhost:8081/api/notifications/summary \
  -H "X-API-KEY: jouw-api-key-hier"
```

Voorbeeld response:

```json
[
  {
    "organizationId": "ziekenhuis-amsterdam",
    "provider": "SWIFTSEND",
    "sent": 142,
    "failed": 3,
    "cancelled": 7,
    "total": 152
  }
]
```

---

## Stap 4 — End-to-end testen via OpenMRS

1. Ga naar http://localhost:8080/openmrs/spa
2. Login met `admin` / `Admin123`
3. Selecteer een patiënt (zorg dat er een telefoonnummer is ingesteld)
4. Maak een afspraak aan voor **morgen**
5. Bekijk in RabbitMQ Management of het bericht in de juiste queue terechtkomt
6. Volg in Grafana de `notifications_sent_total` counter

---

## Configuratie per ziekenhuis

Elke OpenMRS-organisatie heeft een eigen abonnement en eigen tijdzone.
Beide zijn configureerbaar in `RestAPI/src/main/resources/application.properties`:

```properties
# Welke provider per ziekenhuis (NFR 3)
provider.mapping.ziekenhuis-amsterdam=SWIFTSEND
provider.mapping.amsterdam-amc=ASYNCFLOW
provider.mapping.rotterdam-erasmus=LEGACYLINK
provider.mapping.utrecht-umcu=SECUREPOST
provider.mapping.default=SWIFTSEND

# Fallback wanneer primaire provider faalt na 3 retries
provider.fallback=LEGACYLINK

# Tijdzone per ziekenhuis (NFR 13, IANA tz database)
organization.timezone.ziekenhuis-amsterdam=Europe/Amsterdam
organization.timezone.singapore-hospital=Asia/Singapore
organization.timezone.new-york-hospital=America/New_York
organization.timezone.default=Europe/Amsterdam
```

Een nieuw ziekenhuis toevoegen = één regel toevoegen onder beide secties.
Geen code-wijziging nodig.

---

## Een nieuwe provider toevoegen

1. Maak `infrastructure/provider/<naam>/<Naam>Provider.java` die `MessageProvider` implementeert
2. Voeg toe aan `ProviderType` enum
3. Maak een queue + DLQ binding aan in `RabbitMQConfig`
4. Voeg de URL/credentials toe aan `application.properties` en `.env`

De `ProviderDispatcher` pikt nieuwe providers automatisch op via Spring's
`List<MessageProvider>` injectie — geen verdere code-wijziging nodig.

---

## Stoppen

```bash
docker-compose down            # stoppen, data bewaren
docker-compose down -v         # stoppen + database wegen (volume verwijderen)
```

---

## Veelvoorkomende issues

| Probleem | Oplossing |
|---|---|
| `401 Unauthorized` op RestAPI | Vergeet niet de `X-API-KEY` header met de waarde uit `.env` |
| OpenMRS toont geen afspraken-modus | Wacht 2 minuten, OpenMRS heeft tijd nodig om alle modules te laden |
| RabbitMQ queue blijft groeien | Check `notifications.dlq` voor permanent mislukte berichten en de Grafana fout-counter |
| Verkeerde tijdzone in SMS | Controleer of `organization.timezone.<orgId>` is ingesteld voor het betreffende ziekenhuis |

---

## Documentatie

- `docs/adr/` — Architectural Decision Records
- Realisatielogboek
- Technische documentatie
- Realisatielogboek
- `docs/testrapportage.md` — Test-resultaten, coverage, fallback-scenario's
- `saascommunication/README.md` — Documentatie voor OpenMRS beheerders (NFR 2)
