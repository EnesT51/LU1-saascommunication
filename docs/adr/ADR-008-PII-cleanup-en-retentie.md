# ADR-008: PII cleanup en retentie strategie (AVG/GDPR)

## Status

Accepted
Datum: 23-05-2026

## Context

Drie eisen uit de opdracht raken privacy en data-retentie:

- **NFR 5:** *"Authenticatiegegevens voor externe messaging providers worden niet in code of configuratiebestanden opgeslagen. Alle gevoelige gegevens (zoals credentials, tokens en berichteninhoud) dienen te worden versleuteld met minimaal AES-256 voor opslag en TLS 1.3 voor transport. De communicatiemodule mag gevoelige data verwerken, maar mag deze nooit onbeveiligd opslaan, inclusief logbestanden."*
- **NFR 10:** *"De communicatiemodule verwijdert patiënt- en gerelateerde gegevens automatisch binnen 14 dagen na afhandeling van de communicatie."*
- **NFR 11:** *"De communicatiemodule bewaart maximaal een jaar meta-informatie van verstuurde berichten voor traceerbaarheidsdoeleinden. Deze meta-informatie bevat geen direct identificeerbare patiëntgegevens, maar bevat voldoende informatie zodat facturen van messaging providers kunnen worden gecontroleerd."*

Er moet een strategie zijn die deze drie eisen integraal afdekt en die ook door de tests automatisch wordt afgevangen.

## Decision

### Drie-fasen retentie

| Fase | Termijn | Wat gebeurt er |
|---|---|---|
| Actief | 0 – 14 dagen na afspraak einde | Volledige afspraak + notificatie + PII (telefoonnummer, patient ID, naam) staan in DB |
| Geanonimiseerd | 14 dagen – 1 jaar | Afspraken: PII-velden gewist (`patientId = null`, `patientPhoneNumber = null`, `patientName = null`). Notificaties: `appointmentId` geanonimiseerd, provider + status + timestamp blijven voor facturatie |
| Verwijderd | > 1 jaar | Volledige row uit `notifications` tabel verwijderd; afspraken al eerder uit `appointment` tabel via `deleteByEndBefore` |

### Implementatie

1. **`AppointmentCleanupService`** (`@Scheduled` dagelijks)
   - `anonymizeAppointmentsEndedBefore(now - 14 dagen)` — zet PII-velden op null
   - `deleteByEndBefore(now - 365 dagen)` — verwijder oude rows

2. **`NotificationCleanupService`** (`@Scheduled` dagelijks)
   - Anonimiseert `appointmentId` na 14 dagen (alleen statistieken per provider/org/status blijven)
   - Verwijdert rows ouder dan 1 jaar volledig

3. **Logging guard (NFR 5)**
   - `NotificationMessageFactory` logt alleen `appointmentId + providerType`, geen telefoonnummer of berichteninhoud
   - `ApiKeyAuthenticationFilter` redacteert de API-key in logs

4. **Secrets buiten code (NFR 5)**
   - Alle credentials in `.env`, niet in Git (`.gitignore` regel)
   - `application.properties` gebruikt `${SWIFTSEND_API_KEY}` style placeholders

### Wat blijft bewaard voor facturatie (NFR 11)

Na anonimisering bevatten `notifications` rows nog:

- `provider` (welke messaging provider gebruikt is)
- `organization_id` (welk ziekenhuis)
- `status` (SENT/FAILED/CANCELLED)
- `created_at`, `sent_at`, `failed_at` (timestamps)
- `retry_count`

Met deze data kan een beheerder controleren of een factuur van bv. SwiftSend overeenkomt met het aantal succesvol verzonden berichten — zonder dat patiëntgegevens worden bewaard.

### Tests

- `AppointmentCleanupServiceTest` — verifieert dat anonimisering pas na 14 dagen plaatsvindt en hard delete pas na 1 jaar (cutoff-Instant wordt gecaptureerd en gecontroleerd)
- `NotificationCleanupServiceTest` — idem voor notificaties

## Alternatives considered

### Hard delete na 14 dagen (geen anonimisering)

**Afgevallen:** dan kunnen we facturen niet meer controleren (NFR 11 vereist 1 jaar meta-info).

### Encrypt-at-rest met application-level versleuteling

**Afgevallen:** voor MVP gebruiken we de gestandaardiseerde MariaDB encryption-at-rest waar productie-deploys op aansluiten. Application-level versleuteling van PII zou extra complexiteit toevoegen (key rotation, query-performance), terwijl 14-daagse anonimisering al een sterkere garantie biedt: na 14 dagen bestaat de PII gewoon niet meer.

### Vault / Secrets Manager voor `.env`

**Afgevallen voor MVP, aangeraden voor productie.** `.env` werkt voor lokale Docker en student-projecten; in een echte SaaS uitrol moet dit vervangen worden door Vault of AWS Secrets Manager (zie ADR-002 § Configuratie).

## Consequences

### Voordelen

- Drievoudige eis (NFR 5 + 10 + 11) wordt door één samenhangende strategie afgedekt
- Geautomatiseerde scheduled jobs — geen handmatige cleanup nodig
- Tests waarborgen dat de cutoff-windows niet per ongeluk verschuiven bij refactors
- Logs bevatten geen PII, ook niet bij ERROR-stacks

### Nadelen

- Een patiënt die meer dan 14 dagen later een klacht heeft over een SMS, kan niet meer worden terug-gelinkt aan een specifieke afspraak (alleen aan provider + organization + timestamp)
- Cleanup-jobs draaien continu — bij grote backlogs kan dit DB-load veroorzaken (mitigatie: batching in `WHERE end < ? LIMIT 1000`)
- `.env`-strategie is niet productie-grade — moet door beheerder gemigreerd worden naar secrets-manager
