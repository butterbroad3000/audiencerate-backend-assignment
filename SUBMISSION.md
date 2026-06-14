# Submission — [Your Name]

> Please fill this in and include it in your submission (replace the prompts in each section). It helps us understand your thinking and speeds up the follow-up  conversation. Keep it short — bullet points are fine.

## How to build & run
```bash
# 1. start the databases (from databases/)
cd databases && docker compose up -d

# 2. build & run the service (your commands here)
[e.g. ./mvnw clean package && java -jar target/app.jar]
```
- Java version: [e.g. 21.x] · Build tool: [Maven / Gradle]
- Service listens on: [e.g. http://localhost:8080]
- Config: [how the app reads DB settings — env vars / file]
- OpenAPI spec at: [e.g. http://localhost:8080/openapi.json]

## What I built
- [Which endpoints you completed]
- [Any stretch goals you tackled]
- [A sentence or two on the overall result]

## Architecture & stack wiring
- How Jetty + Jersey + Guice fit together: [...]
- How the **three HikariCP pools** are configured, injected and routed: [...]
- How `GET /api/overview` and cross-context endpoints use multiple pools: [...]

## Data access
- JDBC / query approach: [...]
- How you avoid connection leaks and SQL injection: [...]
- Transaction handling (if any): [...]

## Key decisions & trade-offs
- [Notable decisions and why]
- [Anything you deliberately simplified or stubbed, and why]

## What I'd do next (with more time)
- [The first 2–3 things you'd add or improve, and why they matter]

## Notes
- Time spent: [~X hours]
- AI tools used (if any) and for what: [...]

---

## Freelance details
*(This is a freelance engagement — please share these so we can move quickly to the commercial conversation. It does not affect how we score your code.)*

- **Typical daily rate:** [€ / day, net of VAT — state currency]
- **Availability:** [days per week you could commit; earliest start date]
- **Working hours overlap with CET/CEST:** [e.g. full overlap / mornings only]
- **Setup preference:** [fully remote / hybrid / Milan office]
