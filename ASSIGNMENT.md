# Backend Assignment — AudienceRate

Thanks for taking the time to do this. AudienceRate is a tech startup building AI-driven tools that help companies manage their marketing strategy, outbound, and **programmatic** activities. Internally that means customers ingest data from many sources, build **audience segments**, see how well those audiences resolve against
our identity graph (**match rate**), and **activate** them to advertising destinations (Google Ads, Meta, DV360, …).

This exercise asks you to build the **REST API service** behind a slice of that product — backed by the three databases we provide. We work fast, with direct ownership and a strong focus on the quality of what we build, so we care far more about *how* you build this than about how much. We want realistic, production-minded backend work: clean wiring, safe data access, sensible API design.

---

## The stack (required)

Please use exactly this stack, since it mirrors ours:

- **Java 21**
- A **JAX-RS / Jersey**-based REST API on **embedded Jetty**
- **Google Guice** for dependency injection (wire Jersey resources, services and data access through Guice)
- **HikariCP** connection pools — **one per database**, so **three pools** for the three PostgreSQL databases we provide
- **Swagger / OpenAPI** annotations for documentation
- **Maven or Gradle** (your choice) — a single command must build & run the service
- **Git** with a real commit history

Plain JDBC or a light query helper (e.g. JDBI) is fine — your call. **No heavyweight ORM / Spring**; the point is to see you wire this stack yourself.

---

## What we provide

A ready-to-run **PostgreSQL 16 environment with three seeded databases** (`profiles`, `segments`, `activations`) — one `docker compose up` away. See the root **[README.md](./README.md)** and **[databases/README.md](./databases/README.md)** for connection details and the schema; connection settings are in **[.env.example](./.env.example)**.

Because a PostgreSQL connection is bound to a single database, serving all three contexts requires **three separate HikariCP pools** — that multi-datasource wiring is a core part of the exercise. Read connection settings from the environment/config; **don't hardcode credentials**.

---

## What to build

Implement the API described in **[API-CONTRACT.md](./API-CONTRACT.md)**. The pieces
we most want to see:

1. **Service bootstrap** — embedded Jetty serving a Jersey (JAX-RS) application, with resources and dependencies wired via Guice.
2. **Three HikariCP datasources** — one per database, configured (pool size, timeouts) and injected where needed; connections always returned to the pool.
3. **`GET /api/overview`** — aggregates across **all three** databases in one request (the endpoint that exercises every pool).
4. **Segments** — list with **server-side** search / filter / sort / pagination (in SQL), get-by-id, and at least the **create** path with validation and a consistent `400` error model. (Edit / delete if you have time.)
5. **Cross-context composition** — `GET /api/segments/{id}/activations` reads the segment from one database and its activations from another and combines them in your code (no cross-database SQL join).
6. **`data-sources`** (profiles db) and **`destinations`** (activations db) reads.
7. **OpenAPI** annotations with a served spec (e.g. `/openapi.json`).
8. **Health** endpoint that checks all three pools.

> Priority: a clean vertical slice — bootstrap + 3 pools + overview + segments list/get + create — done well beats every endpoint done shakily. If you run out of time, leave the rest stubbed and note it.

---

## Stretch goals (optional, pick what interests you)

- **Tests** — unit tests for your services, and/or integration tests against a real database (Testcontainers is a strong signal).
- **Transactions** done right for multi-statement writes (e.g. segment + tags).
- **Graceful shutdown** that drains Jetty and closes the pools.
- **Observability** — structured logging, request logging, basic pool metrics, or a richer health/readiness split.
- **Keyset (cursor) pagination** as an alternative to offset.
- **Resilience** — timeouts, sensible behaviour when one database is down (e.g. `/api/overview` degrades rather than 500s entirely).
- DB migrations (Flyway/Liquibase) for any tables you add for your own writes.

Don't do all of these — depth over breadth.

---

## What we're evaluating

- **Stack wiring (Jersey / Jetty / Guice)** — clean bootstrap, resources and collaborators injected through Guice, no hand-rolled `new` where DI is expected.
- **Multi-datasource / HikariCP** — three pools, sensibly configured and injected; connections never leak (try-with-resources); correct per-database routing.
- **API design & correctness** — endpoints match the contract; right status codes; pagination/filter/sort/search performed in SQL; validation; consistent error model.
- **SQL & data-access safety** — parameterised queries (no string-concatenated SQL / injection), reasonable queries, no obvious N+1.
- **OpenAPI** — accurate annotations and a served spec.
- **Code quality & structure** — clear layering (resource / service / dao), readable, little duplication, no dead code.
- **Robustness & ops** — config from env (no secrets in code), graceful startup/shutdown, useful logging, health across pools.
- **Engineering hygiene** — meaningful commits, a clear README, builds & runs first try.

We are **not** judging how many endpoints you finish. Correct, safe, well-structured code on the core slice wins.

---

## Time & scope

Please cap this at roughly **6 hours** (the stack setup is real work — budget for it and prioritise the vertical slice). If you run out of time, stop and write in your README what you'd do next and why — we read that closely. Use AI tools if you normally do; just be ready to explain any line of your submission.

---

## Submitting

1. A self-contained project (Maven or Gradle) that builds and runs with a single, documented command (e.g. `./mvnw clean package && java -jar target/app.jar`, or a run plugin). It should connect to the provided databases on `localhost:5432`.
2. Fill in the provided **[SUBMISSION.md](./SUBMISSION.md)**: how to build & run, key decisions & trade-offs, what you'd improve with more time, and — since this is a freelance engagement — your **typical daily rate** and availability (CET/CEST).
3. Send us a zip or a link to a git repo (with commit history). Don't commit secrets; include a `.env.example` if your app needs config.

Questions about the task are welcome — email us; asking good questions is a positive signal, not a negative one. Have fun with it.
