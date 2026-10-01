# GrabSeat — event booking service

Ticketmaster-style event booking service built with Spring Boot 3 + Maven + PostgreSQL.

## Prereqs
- Java 21+ (you have 25, targets 21)
- Maven 3.9+
- Docker (for Postgres) or local Postgres 16

## Quick start

```powershell
# 1. Start Postgres
docker compose up -d

# 2. Run app
mvn spring-boot:run

# 3. Check health
# http://localhost:8080/api/health
```

## Structure
- `com.grabseat` — app entry
- `com.grabseat.controller` — REST controllers
- `application.properties` — Postgres + JPA config

## Next
Drop your full scope md file here (e.g. `docs/SCOPE.md`) and we'll implement:
events, venues, seats, bookings, users/auth.
