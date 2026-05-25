# NFR Checklist — Presentatie Voorbereiding

## NFR 1 — Authenticatie via API-key
**Status: ✅ GEDAAN**
- RestAPI `ApiKeyAuthenticationFilter` valideert `X-API-KEY` header op elke request
- API-key in `.env`, niet in code
- Ongeautoriseerde requests krijgen 401 Unauthorized

## NFR 2 — Documentatie OpenMRS beheerders
**Status: ✅ GEDAAN**
- `saascommunication/README.md` — installatie, configuratie, troubleshooting
- Inleiding op hoe de module werkt en wat het doet

## NFR 3 — Support 4 messaging providers
**Status: ✅ GEDAAN**
- SwiftSend → HTTP GET met SMS-data
- LegacyLink → HTTP POST XML (CDATA voor speciale tekens)
- AsyncFlow → HTTP POST JSON
- SecurePost → OAuth 2.0 token + HTTP POST
- Elke provider has eigen `*Provider.java` implementatie van `MessageProvider` interface

## NFR 4 — Fallback providers
**Status: ✅ GEDAAN**
- `OrganizationProviderMapper.resolveFallbacks()` → geeft geordende lijst fallback providers
- `provider.fallback=LEGACYLINK` in `application.properties`
- Bij falen primaire provider → probeert fallbacks in volgorde

## NFR 5 — Encryptie & credentials niet in code
**Status: ✅ GEDAAN**
- **Credentials:** Allemaal in `.env`, niet in code of `application.properties`
- **Transport TLS:** 
  - ActiveMQ → RestAPI: **TLS 1.3 op poort 61617** ✅
  - RabbitMQ → RestAPI: **TLS 1.3 op poort 5671** ✅
  - Provider HTTP calls: **HTTPS** (externe providers) ✅
- **Logging:** `NotificationMessageFactory` logt geen PII (alleen appointmentId + providerType)
- **PII cleanup:** Na 14 dagen wordt PII gewist (sterker dan encryptie)

## NFR 6 — Retry mechanisme
**Status: ✅ GEDAAN**
- Primaire provider: **3 retries met exponential backoff** (1s, 2s, 4s)
- `ProviderDispatcher.tryProviderWithRetries()` implementeert dit
- Tussen retries: `Thread.sleep()` met exponential backoff

## NFR 7 — Downtime handling via fallback
**Status: ✅ GEDAAN**
- Primaire provider 3x mislukt → probeer fallback1 3x → fallback2 3x → fallback3 3x
- Scenario H (van requirements): **totaal 12 HTTP-pogingen voor 4 providers**
- Tests: `ProviderDispatcherTest.dispatchRetriesPrimaryThenFallbacksWhenPrimaryFails()`

## NFR 8 — FHIR R4 validatie
**Status: ✅ GEDAAN**
- HAPI FHIR validator valideert inkomende Appointment payload tegen R4 structuur
- `AppointmentValidationService.validateFhirAppointment()`
- Ongeldige FHIR → exception → afspraak niet opgeslagen

## NFR 9 — Afspraken opslaan
**Status: ✅ GEDAAN**
- `AppointmentService.saveAppointment()` slaat FHIR Appointment op in MariaDB
- Schema: `appointments` tabel met patientId, phoneNumber, start, end, organizationId, etc.
- Queries per organisatie en status ondersteund

## NFR 10 — PII cleanup na 14 dagen
**Status: ✅ GEDAAN**
- `AppointmentCleanupService` (@Scheduled dagelijks):
  - `anonymizeAppointmentsEndedBefore(now - 14d)` → zet patientId, phoneNumber, name = null
  - `deleteByEndBefore(now - 365d)` → hard delete na 1 jaar
- Tests: `AppointmentCleanupServiceTest` verifieert timing

## NFR 11 — Meta-info retentie 1 jaar
**Status: ✅ GEDAAN**
- Na anonimisering blijven `notifications` rows met:
  - `provider` (welke SMS-provider)
  - `organization_id` (welk ziekenhuis)
  - `status` (SENT/FAILED/CANCELLED)
  - `created_at`, `sent_at`, timestamps
  - `retry_count`
- **Facturatie:** Beheerder kan SwiftSend factuur controleren tegen `notifications where provider='SWIFTSEND' and status='SENT'`

## NFR 12 — Observability
**Status: ✅ GEDAAN**
- **Metrics:** Micrometer counters
  - `notifications_sent_total` — aantal succesvol verzonden
  - `notifications_failed_total` — aantal mislukt
  - Per provider + org + status
- **Tracing:** OpenTelemetry exporteert naar OTLP gRPC (lgtm:4317)
- **Dashboard:** Grafana met saascommunication dashboard (throughput, error rate, queue depth)
- **Logs:** Structured logging (JSON) via OpenTelemetry, queryable in Loki

## NFR 13 — Tijdzone per organisatie
**Status: ✅ GEDAAN**
- `organization.timezone.<organizationId>=<IANA-timezone>` in `application.properties`
- `NotificationScheduler` zet reminders in **lokale tijd** van het ziekenhuis
- Voorbeeld:
  - `organization.timezone.ziekenhuis-amsterdam=Europe/Amsterdam`
  - `organization.timezone.singapore-hospital=Asia/Singapore`
- Tests: `TimezoneConversionTest` verifieert correctie conversie

---

## Samenvatting

| # | Requirement | Status | Bewijs |
|---|---|---|---|
| 1 | API-key auth | ✅ | `ApiKeyAuthenticationFilter` + tests |
| 2 | Documentatie | ✅ | `saascommunication/README.md` |
| 3 | 4 providers | ✅ | 4 implementaties + tests |
| 4 | Fallback | ✅ | `OrganizationProviderMapper` |
| 5 | TLS + geen credentials | ✅ | `.env` + ActiveMQ/RabbitMQ TLS 1.3 |
| 6 | Retry | ✅ | 3 retries exponential backoff |
| 7 | Downtime handling | ✅ | Cross-provider fallback chain |
| 8 | FHIR R4 validatie | ✅ | HAPI validator + tests |
| 9 | Afspraken opslaan | ✅ | MariaDB `appointments` tabel |
| 10 | PII cleanup 14d | ✅ | `AppointmentCleanupService` |
| 11 | Meta-info 1 jaar | ✅ | `notifications` tabel met facturatie-data |
| 12 | Observability | ✅ | OTel + Prometheus + Grafana + Loki |
| 13 | Timezone per org | ✅ | `organization.timezone.*` config |

**Totaal: 13/13 ✅ COMPLEET**
