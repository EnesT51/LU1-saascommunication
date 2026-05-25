# ADR 4 — Keuze voor queuing systeem

**Status:** Voorgesteld  
**Datum:** 21-05-2026  

---

## Context
We hebben een queue systeem nodig, zodat modules onafhankelijk van elkaar kunnen functioneren en integraties met messaging systemen (zoals SWIFT) mogelijk blijven zonder sterke afhankelijkheden.

---

## Functionele eisen
- Asynchrone verwerking van berichten  
- Ondersteuning van verschillende message types (bijv. events, notificaties, integraties)  
- Mogelijkheid om berichten te routeren naar specifieke consumers  
- Ondersteuning voor retries en foutafhandeling  

---

## Niet-functionele eisen
- Hoge betrouwbaarheid (berichten mogen niet verloren gaan)  
- Schaalbaarheid bij toenemende belasting  
- Lage coupling tussen producers en consumers  
- Ondersteuning voor multi-tenant gebruik  
- Beveiligde communicatie (encryptie en toegangscontrole)  
- Monitoring en beheer van message flows  

---

## Opties

### RabbitMQ
**Voordelen:**
- Betrouwbare message delivery (acknowledgements en persistence)  
- Ondersteuning voor routing via exchanges  
- Goede ondersteuning voor retries en dead-letter queues  
- Relatief eenvoudig in gebruik en beheer  
- Geschikt voor verschillende messaging patronen  

**Nadelen:**
- Extra infrastructuur en beheer noodzakelijk  
- Monitoring en onderhoud vereist  

---

### ActiveMQ
**Voordelen:**
- Betrouwbare messaging  
- Ondersteuning voor queues en pub/sub  

**Nadelen:**
- Minder modern en minder actief doorontwikkeld  
- Complexere configuratie  
- Lagere performance dan nieuwere oplossingen  

---

### Apache Kafka
**Voordelen:**
- Zeer hoge schaalbaarheid  
- Persistent opslag van events  

**Nadelen:**
- Complex in setup en beheer  
- Minder geschikt voor traditionele queue use-cases  

---

## Decision
Wij kiezen voor **RabbitMQ** als messaging broker, omdat het moderner is dan ActiveMQ en minder complex is dan Apache Kafka.

---

## Consequences

### Voordelen
- We voorkomen dat berichten verloren gaan  
- Systemen blijven onafhankelijk  
- Meerdere subscribed systemen kunnen berichten ontvangen of elkaar vervangen  

### Nadelen
- Extra infrastructuur  
- Monitoring en onderhoud  
- Berichten worden niet direct verwerkt (eventual consistency)  
- Kans op schema drift  
``
