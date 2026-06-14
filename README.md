# AudienceRate — Backend Take-Home

Welcome, and thanks for interviewing with us. This repository contains everything you need for the backend assignment: a ready-to-run **PostgreSQL environment with three seeded databases**, and a precise **API contract** to implement.

You will build the **REST API service**. We do not provide any starter app — wiring the stack together is part of what we're evaluating.

## What's in here

```
.
├── ASSIGNMENT.md        ← read this first: what to build & how we evaluate
├── API-CONTRACT.md      ← the endpoints, request/response shapes & status codes
├── SUBMISSION.md        ← fill this in and send it back with your code
├── .env.example         ← the connection details your service should read
└── databases/
    ├── docker-compose.yml   ← one command to start the 3 seeded databases
    ├── README.md            ← schema reference & how to reset
    ├── generate_seed.py     ← how the seed was produced (FYI; you don't need it)
    └── init/                ← schema + seed SQL (mounted by docker-compose)
```

## Quick start — the database environment

**Option A — Docker (recommended):**
```bash
cd databases
docker compose up -d           # starts Postgres 16 with 3 databases on :5432
```
This creates and seeds three databases on a single Postgres server:

| Database      | Bounded context                  | JDBC URL                                     |
|---------------|----------------------------------|----------------------------------------------|
| `profiles`    | data sources / identity counts   | `jdbc:postgresql://localhost:5432/profiles`    |
| `segments`    | audience segments, tags, trend   | `jdbc:postgresql://localhost:5432/segments`    |
| `activations` | destinations & activations       | `jdbc:postgresql://localhost:5432/activations` |

User / password: `audiencerate` / `audiencerate`. See `.env.example`.

> Because a PostgreSQL connection is bound to a single database, talking to all three means **three separate connection pools** — which is exactly the HikariCP setup we want to see (see ASSIGNMENT.md).

**Option B — your own Postgres:** run the scripts in `databases/init/` (in order: `00`, `01`, `02`, `03`) against a local Postgres instance.

To reset to a clean dataset: `docker compose down -v && docker compose up -d`.

## The stack we ask for

Java 21 · JAX-RS / Jersey · embedded Jetty · Google Guice (DI) · HikariCP (3 pools) · Swagger / OpenAPI annotations.

Full task details and evaluation criteria are in **[ASSIGNMENT.md](./ASSIGNMENT.md)**;
the endpoints to implement are in **[API-CONTRACT.md](./API-CONTRACT.md)**.

Good luck — and reach out if anything is unclear.
