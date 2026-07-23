# Day 4 — Solved Files & How To Run

Day 4 is the persistence + web-plumbing day. You wired the JPA
`TradeSpecifications`, brought the `TradeService` CRUD to life,
gave the app a proper Swagger doc, a real DB health indicator,
and RFC-7807 `ProblemDetail` responses for every domain exception.

**In this file:**

1. One-line copy command.
2. Which Day-4 tickets ship as code vs starter vs config-only.
3. File-by-file map.
4. Step-by-step run guide (JDK 21 + H2, no Docker needed).
5. What success looks like + troubleshooting.

---

## Quick start

Paths are relative to the **project root** (contains `backend/`, `docker-compose.yml`, `day4-solved-files/`).

```bash
# From the project root — one-shot overlay:
cp -R day4-solved-files/backend/ backend/
```

---

## Scope

Day 4 has 15 tickets (ADV048–062). Only five files needed code
changes — many tickets are pom-only dependencies (Envers, springdoc,
opencsv) or profile config that already ships in the starter.

| Ticket | Status | Where |
|---|---|---|
| ADV048/049 — Spring profile YAML + dev/uat/prod overrides | ✓ already in starter | `application-*.yml` |
| ADV050 — Trade JPA entity + auditing | ✓ already in starter | `Trade.java` |
| ADV051 — Spring Data repositories | ✓ already in starter | `*Repository.java` |
| ADV052 — Envers | ✓ dependency in starter | pom.xml |
| ADV053/054 — DTO records + mapper | ✓ already in starter | `dto/*.java` |
| ADV055 — Custom JPQL filter query | ✓ already in starter | `TradeRepository.findByFilters` |
| ADV056 — Specification-based dynamic queries | ✓ in this folder | `TradeSpecifications.java` |
| ADV057 — Pageable / Page<T> | ✓ already wired | `TradeRepository`, `TradeController` |
| ADV058 — Swagger OpenAPI bean + bearerAuth | ✓ in this folder | `OpenApiConfig.java` |
| ADV059 — Custom `DatabaseHealthIndicator` | ✓ in this folder | `DatabaseHealthIndicator.java` |
| ADV060 — CSV export | ✓ already in starter | `TradeController.exportCsv` |
| ADV061 — Multipart CSV import | ✓ already in starter | `TradeController.importCsv` |
| ADV062 — RFC-7807 ProblemDetail | ✓ in this folder | `GlobalExceptionHandler.java` |
| ADV064–067 — TradeService CRUD + softDelete | ✓ in this folder | `TradeService.java` |

---

## File-by-file map

| # | File in `day4-solved-files/` | Paste into | Tickets |
|---|-----------------------------|-----------|---------|
| 1 | `backend/…/repository/TradeSpecifications.java` | same path | ADV056 |
| 2 | `backend/…/config/OpenApiConfig.java` | same path | ADV058 |
| 3 | `backend/…/observability/DatabaseHealthIndicator.java` | same path | ADV059 |
| 4 | `backend/…/exception/GlobalExceptionHandler.java` | same path | ADV062 |
| 5 | `backend/…/service/TradeService.java` | same path | ADV064–067 |

---

## What each change does

- **`TradeSpecifications.java`** — three `Specification<Trade>` factories (`hasStatus`, `tradeDateBetween`, `hasCounterparty`). Each returns `cb.conjunction()` when its filter is `null`, so callers can compose them freely without pre-checking for nulls.
- **`OpenApiConfig.java`** — the `reconxOpenAPI()` `@Bean` sets title, version, description, contact, and registers a `bearerAuth` HTTP scheme so Swagger UI shows an "Authorize" button that accepts JWTs (green-lit for Day-5 security).
- **`DatabaseHealthIndicator.java`** — implements `doHealthCheck()` with a 2-second-timeout `SELECT 1`, records `latencyMs` as a detail. Any thrown exception bubbles up and Spring converts it to `DOWN`.
- **`GlobalExceptionHandler.java`** — maps each domain exception to the right HTTP status: `TradeNotFound → 404`, `DuplicateTradeRef → 409`, `InvalidTrade → 400`, `ReconciliationMismatch → 422`, plus JSR-380 `MethodArgumentNotValidException` and `ConstraintViolationException` → 400 with a readable joined message.
- **`TradeService.java`** — six methods: `create` (duplicate-check + build + save + metrics + event), `update`, `updateStatus`, `softDelete` (calls `t.softDelete()` which sets `deleted_at`), and `list` composing the three Specifications with `.where(...).and(...).and(...)`.

---

## Run the project

### Before you start

1. **Java 21.** `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` on macOS.
2. **You're in the project root.**
3. **You copied the solved files.** If not: `cp -R day4-solved-files/backend/ backend/`
4. **Days 1–3 are applied** (Day 4 depends on the Day-1 audit-log fix, the Day-2 exceptions, and the Day-3 recon engine):
   ```bash
   cp -R day1-solved-files/backend/ backend/
   cp -R day2-solved-files/backend/ backend/
   cp -R day3-solved-files/backend/ backend/
   ```

### Step 1 — Compile

```bash
cd backend
./mvnw -q clean compile
echo "exit=$?"     # want exit=0
```

### Step 2 — Boot on H2

```bash
./mvnw spring-boot:run
```

Watch for `Started ReconxApplication in ~4 seconds`. In a second terminal:

```bash
curl http://localhost:8081/api/actuator/health
# → {"status":"UP","groups":["liveness","readiness"]}

curl http://localhost:8081/api/actuator/health/database
# → {"status":"UP","details":{"latencyMs":<n>}}   ← ADV059 in action

open http://localhost:8081/api/swagger-ui.html
# → Title reads "ReconX API", green "Authorize" button appears ← ADV058
```

Try a bad `POST /api/v1/trades` (e.g. missing `quantity`) — the response
should be an RFC-7807 JSON body with `title`, `status: 400`, and a
`detail` listing the field errors. That's ADV062.

Hit `Ctrl+C` when you're done.

---

## What success looks like

- `./mvnw clean compile` exits `0`.
- Boot reaches `Started ReconxApplication`.
- `/actuator/health` and `/actuator/health/database` both return `UP`.
- Swagger UI at `/api/swagger-ui.html` shows the customised title + Authorize button.
- A malformed POST returns a ProblemDetail JSON body with the field errors joined by `; `.

---

## If something goes wrong

- **"Cannot find symbol: SecurityRequirement / OpenAPI"** → springdoc dependency missing. Confirm `springdoc-openapi-starter-webmvc-ui` is on the classpath (it is by default in this project).
- **"Cannot find symbol: TradeEvent.EventType"** → Day 4 depends on Day 9's DTO. That DTO already ships in the starter; if you deleted it, run `cp -R day1-solved-files/backend/ backend/` to restore.
- **Boot fails on Hibernate schema validation for `audit_log`** → Day-1 fix missing. Overlay Day 1.
- **`ConstraintViolationException` handler doesn't fire** → validation isn't being triggered on that endpoint. Add `@Valid` to the controller parameter.
- **Port 8081 in use** → `lsof -i :8081` then `kill <PID>`.

Good progress — you're over the persistence hump. Onward to security.
