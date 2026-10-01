# GrabSeat — Delivery Plan

Source: `requirements/requirements.md` + `requirements/FinalArchitecture.png`
Stack: Spring Boot 3.5.6 + Maven (Java 21, runs on JDK 25) + PostgreSQL
Strategy: modular monolith first (one story = one git commit), evolve to final architecture
  (API Gateway + Event/Search/Booking + Redis + Elasticsearch + CDC/Kafka + Stripe mock).
Last updated: 2026-10-01 (UTC)

Status legend: `Done` | `In Progress` | `Not Started`

## Execution status

| ID | User story / task | Maps to requirements | Status | Evidence / notes |
|----|-------------------|----------------------|--------|------------------|
| S0 | Project scaffold: Spring Boot, JPA, Postgres, `docker-compose.yml`, `GET /api/health` | Tech guidelines 2,3 | Done | `pom.xml`, `docker-compose.yml`, `HealthController.java`; verified `mvn -q compile -DskipTests` OK |
| US0 | Seed DB with movies + comedy shows | New request (demo data for 1-4) | Done | Merged PR #1; live-verified in Docker: `GET /events/1` = Dune show, 40 tickets |
| US1 | View event: `GET /events/:eventId -> Event & Venue & Performer & Ticket[]` | Core 1; API 1 | Done | Merged PR #2; `EventServiceTest` + `EventControllerTest` 2/2 pass; live-verified in Docker |
| US2 | Search events: `GET /events/search?keyword,start,end,pageSize,page` | Core 2; API 2 | Done | Merged PR #4; Specifications-based `EventService.search` + `GET /events/search`; 4/4 pass; live-verified in Docker; ES + CDC/Kafka later |
| US3 | Reserve tickets: `POST /bookings/reserve {ticketIds[]}` | Core 3; API 3 | Done | Multi-ticket `Booking` + `ReserveTicketsRequest/Response` pairs; JWT principal; pessimistic lock + 201; 18/18 pass; live-verified in Docker; Redis lock + queue later |
| US4 | Confirm booking: `POST /bookings/confirm {bookingId,cardNumber,expMonth,expYear,cvc}` | Core 3; API 4 | Done | `ConfirmBookingRequest/Response` pairs + `DummyStripeService` (4242 ok, 4000…0002 decline → 402); `paymentReference` in response; 22/22 pass; live-verified in Docker |
| US5 | View my bookings: `GET /users/:userId/bookings` | Core 4 | Not Started | Needs `Booking(id,userId,tickets)` entity |
| US6 | Admin add events + scheduled runs (ADMIN) | Core 5 | Done | Roles in JWT + seeded admin; `POST /admin/venues|performers|events|events/schedule` (201); runs materialize dated shows (range, daily timings, duration, overlap check, seriesId); SPA Admin page; 49/49 pass; live-verified in Docker |
| US7 | Dynamic pricing for popular events | Core 6 | Not Started | Rule TBD (e.g. sold% >80% → surge multiplier); apply on view/reserve |
| INFRA-1 | Postgres + Redis + Elasticsearch + Kafka/Debezium in Docker Desktop | Tech 2,3; FinalArchitecture | In Progress | Docker: `Dockerfile` (multi-stage Maven+JRE21) + compose `app` + `postgres` healthy; verified `/api/health` UP + `/events/1` 40 tickets; pending: Redis, ES, Kafka |
| TEST | Unit + integration (Testcontainers) + e2e per story | Tech 4 | In Progress | CI `.github/workflows/ci.yml` runs `mvn -B test` on push/PR with Postgres 16 service; verified locally 3/3 pass incl. `contextLoads`; pending: Testcontainers, e2e |
| UI | React SPA: login, event list, event detail, booking, payment | Tech 5 | In Progress | `frontend/` (Vite + Router); public browse, JWT login-gated reserve→pay→confirm with token reuse; `web` compose service + nginx proxy; CORS for Vite dev |
| AUTH-1 | User registration + password login (persisted) | New request | In Progress | `users` table (id internal; login unique; fullName; email unique for booking mail); `POST /auth/register` 201 + `POST /auth/login` 401; JWT sub=id plus userId/login claims; bookings keyed by internal id; SPA register page |

## Plan of action (in order)

1. Commit S0+US0 (verify with live Postgres: `docker compose up -d`, `mvn spring-boot:run`, check 6 events seeded).
2. US1 → US6 → US2(DB) → US3+US4 → US5 → US7 — each as separate commit.
3. INFRA-1: add Redis cache/lock, ES + CDC/Kafka indexer, Gateway (auth/rate-limit/routing).
4. TEST + UI alongside each story after US1.
5. `docker compose up` demo + e2e.

## Open decisions

- Search params: unify `keyword/start/end` vs `term/location/type/date`.
- `reserve/confirm` REST shape (proposed above).
- Dynamic pricing rule + `Booking` schema from diagram.
- Git: `US0` merged into `feature/US1-view-event`; each story stays on its own `feature/*` branch.
- Branches created: `S0, US0-US7, INFRA-1, TEST, UI` (all `feature/*`).

## Next action

US6 done — next Elasticsearch-powered search.
