# ADR-007: Per-organisatie provider mapping en fallback strategie

## Status

Accepted
Datum: 23-05-2026

## Context

NFR 3 stelt: *"Organisaties dienen gebruik te kunnen maken van één van de volgende messagingproviders die alle 4 door de communicatiemodule worden ondersteund: SwiftSend, LegacyLink, AsyncFlow, SecurePost."*

NFR 7 stelt: *"Downtime bij communicatieproviders of OpenMRS instanties dient te worden opgevangen door een zelfontworpen en gedocumenteerde fallback- of retrymechanisme."*

We moeten een mechanisme ontwerpen dat:

- Elke organisatie aan exact één primaire provider koppelt
- Toevoegen van een nieuwe organisatie zonder code-wijziging mogelijk maakt
- Bij falen van de primaire provider terugvalt op een andere provider
- Onbekende organisaties niet laat falen, maar terugvalt op een redelijke default

## Decision

### Configuratie in `application.properties`

```properties
# Default voor onbekende organisaties
provider.mapping.default=SWIFTSEND

# Per organisatie
provider.mapping.ziekenhuis-amsterdam=SWIFTSEND
provider.mapping.amsterdam-amc=ASYNCFLOW
provider.mapping.rotterdam-erasmus=LEGACYLINK
provider.mapping.utrecht-umcu=SECUREPOST

# Fallback provider bij provider-falen
provider.fallback=LEGACYLINK
```

### `OrganizationProviderMapper`

Een Spring `@ConfigurationProperties(prefix = "provider")` component:

- `resolvePrimary(organizationId)` → geeft de primaire `ProviderType` terug
- `resolveFallbacks(primary)` → geeft een geordende lijst van fallback providers terug, met de geconfigureerde `provider.fallback` voorop, gevolgd door de resterende providers

### Retry & fallback flow

1. Notificatie wordt voor primaire provider gequeued in `<provider>.queue`
2. `ProviderDispatcher` doet HTTP call naar de externe provider
3. Bij retryable failure (HTTP 5xx, timeout, netwerkfout) → Spring AMQP retry-policy probeert 3 keer
4. Provider permanent down -> 3 retries -> fallback 3 retries ander provider -> fallback 3 retries ander provider -> fallback 3 retries ander provider → bericht gaat naar `<provider>.dlq`
5. Notification status → `FAILED` met `failure_reason`
6. Een `@Scheduled(fixedRate = 30 min)` retry job probeert FAILED notifications opnieuw via de fallback provider
7. Bij permanente failure (4xx) → meteen FAILED, geen retry

### Bij onbekende organisatie

`OrganizationProviderMapper.resolvePrimary()` valt automatisch terug op `provider.mapping.default` (SWIFTSEND). Geen code-wijziging nodig om een nieuw ziekenhuis aan te sluiten — pas later config aan als ze een eigen provider willen.

## Alternatives considered

### Hardcoded switch-case in code

```java
switch (orgId) {
    case "ziekenhuis-amsterdam": return new SwiftSendProvider();
    ...
}
```

**Afgevallen:** schendt Open/Closed principle, elke nieuwe organisatie = code-wijziging + redeploy.

### Database tabel voor mappings

**Voordelen:** runtime-aanpasbaar zonder restart

**Afgevallen:** voor deze schaal (< 100 organisaties) is property-config voldoende; database voegt operationele complexiteit toe; bij wijziging is een config-reload of restart acceptabel. Wel een goede toekomstige uitbreiding zodra het systeem multi-tenant SaaS in productie gaat.

### Round-robin / load-balanced over alle providers

**Afgevallen:** elke organisatie heeft een eigen abonnement bij één specifieke provider; round-robin zou betekenen dat ze meerdere abonnementen moeten betalen.

## Consequences

### Voordelen

- Nieuwe organisatie aansluiten = één regel toevoegen in `application.properties`
- Default-fallback zorgt dat onbekende organisaties niet falen
- Per-provider isolatie via RabbitMQ queues (zie ADR-005) zorgt dat één trage provider de anderen niet blokkeert
- Retry + fallback dekken zowel tijdelijke storingen als langduriger uitval

### Nadelen

- Wijziging van mappings vereist herstart van RestAPI (of Spring `@RefreshScope` toevoegen)
- Geen UI om mappings te beheren — beheerder moet `.properties` editen
- Fallback gaat naar één enkele provider; bij parallelle fallback-strategieën zou een matrix nodig zijn
