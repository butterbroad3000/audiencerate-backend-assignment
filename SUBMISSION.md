# Submission — Evgeni

> Please fill this in and include it in your submission (replace the prompts in each section). It helps us understand your thinking and speeds up the follow-up  conversation. Keep it short — bullet points are fine.

## How to build & run
```bash
# 1. start the databases (from databases/)
cd databases && docker compose up -d

# 2. build & run the service (your commands here)
mvn clean package && java -jar target/audiencerate-backend-1.0.0.jar     
```
- Java version: 21 · Build tool: Maven
- Service listens on: http://localhost:8080
- Config: env vars (see `.env.example`); defaults to `localhost:5432` with user/pass `audiencerate`
- OpenAPI spec at: http://localhost:8080/openapi.json

## What I built
- 13 endpoints: health, overview, segments CRUD, segment trend, data-sources, destinations, activations list + create
- Cross-database work — `GET /api/segments/{id}/activations` goes segments DB → activations DB → enriches in Java, since the databases are separate and JOINs aren't possible
- Same error shape everywhere: `{ "error": { "code", "message", "details" } }`. `details` is keyed by field name, so a frontend knows exactly which input to highlight
- Swagger on all resources, spec at `/openapi.json`, UI at `/swagger-ui`
- Also: transactions with rollback, graceful shutdown of all three pools, 43 tests (JUnit 5 + Mockito + Instancio)

## Architecture & stack wiring
- Jetty 12 starts embedded from `main()` — no separate install. Jersey 3.x sits as a `ServletContainer` on `/*`
- Guice 7 wired across three modules: `DatabaseModule` (3 HikariCP pools), `DaoModule` (5 DAOs → correct pool), `ServiceModule` (6 services + validator). Guice and Jersey don't integrate out of the box, so resources get `injector.getInstance()` and are registered into `ResourceConfig` by hand. Slightly tedious but there's no magic — you can trace exactly what gets created
- One HikariCP pool per database, routed with `@BindingAnnotation`: `@ProfilesDb`, `@SegmentsDb`, `@ActivationsDb`. Each DAO's constructor gets the pool it needs. `GET /api/overview` hits all three DAOs and stitches results together in Java

## Data access
- Plain JDBC through `PreparedStatement`. No query concatenation — all user input goes through `ps.setString()` / `setLong()` / `setObject()`. Dynamic `WHERE` clauses are built with `?` placeholders, never string interpolation
- Sort columns run through a whitelist (`ALLOWED_SORTS`) before they reach `ORDER BY`
- Connections via try-with-resources everywhere. Transaction helper is `executeInTransaction()` — opens connection, `autoCommit(false)`, runs the lambda, commits or rolls back. Both `SegmentDao.create()` and `update()` share this helper, so there's exactly one pattern
- PostgreSQL `json_agg` arrays parsed with Jackson — `ObjectMapper` comes through DI, not `new`

## Key decisions & trade-offs
- Records for most DTOs: `DataSourceInfo`, `Destination`, `Overview`, `SegmentTrendPoint`, `HealthStatus`. `Segment` and `Activation` are plain POJOs because `mapRow` uses setters
- Raw JDBC rather than JDBI or MyBatis. The extra verbosity means every query is visible in the code. For ~10 queries the trade-off is fine
- Offset pagination. At 36 segments it's a non-issue. If the dataset grows past 100k I'd move to keyset
- `DestinationDao.findAll()` instead of per-ID lookups. There are 7 destinations total — one query beats seven
- `ObjectMapper` through Guice DI. It's just `new ObjectMapper()` but consistent with how everything else is wired. Same logic for `JacksonFeature` — registered explicitly rather than auto-discovered

## What I'd do next (with more time)
1. Request-logging filter — method, path, status, latency on every request
2. Split health into liveness vs readiness — right now it's one endpoint checking all three pools
3. Degrade gracefully in `/api/overview` when one DB is down, rather than 500
4. Testcontainers — integration tests against real PostgreSQL instead of mocking DAOs

## Notes
- Time spent: ~6 hours
- AI tools used: Claude Code — architecture, boilerplate, code review, tests, docs and comments

---

## Freelance details
*(This is a freelance engagement — please share these so we can move quickly to the commercial conversation. It does not affect how we score your code.)*

- **Typical daily rate:** [€ / day, net of VAT — state currency]
- **Availability:** [days per week you could commit; earliest start date]
- **Working hours overlap with CET/CEST:** [e.g. full overlap / mornings only]
- **Setup preference:** [fully remote / hybrid / Milan office]
