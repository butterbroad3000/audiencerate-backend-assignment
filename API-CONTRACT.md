# API Contract — implement this

Base path: `/api`. All responses are JSON. This is the contract your service should fulfil; the **(db)** tag on each endpoint shows which database(s) back it — note how several endpoints span more than one, which is the point of the 3-pool setup.

## Conventions
- List endpoints return `{ "data": [...], "pagination": {...} }`.
- Single-resource endpoints return `{ "data": {...} }`.
- Errors return a consistent envelope:
  `{ "error": { "code": <int>, "message": <string>, "details"?: {<field>: <msg>} } }`.
- Use appropriate status codes (`200`, `201`, `204`, `400`, `404`, `500`).
- Field names in JSON should be `camelCase` (e.g. `audienceSize`), even though the columns are `snake_case`.

---

## Required endpoints

### `GET /api/health`  *(all 3 dbs)*
Liveness + readiness. Should verify each of the three pools can serve a connection.
```json
{ "status": "ok", "databases": { "profiles": "up", "segments": "up", "activations": "up" } }
```

### `GET /api/overview`  *(all 3 dbs)*
Aggregates across every context — this is the endpoint that should touch all three pools in a single request.
```jsonc
{
  "kpis": {
    "totalProfiles": 29013000,        // sum(profiles_count) from profiles
    "totalSegments": 36,              // from segments
    "activeSegments": 14,             // from segments
    "avgMatchRate": 0.701,            // avg(match_rate) from segments
    "identitiesResolved": 20338113,   // totalProfiles * avgMatchRate (your call how to derive)
    "totalActivations": 19            // from activations
  },
  "segmentsByStatus": { "active": 14, "draft": 12, "archived": 10 },
  "topSegments": [ { "id": "seg_0004", "name": "Auto Intenders", "audienceSize": 4018500 } ]
}
```

### `GET /api/segments`  *(segments db)*
Paginated list with **server-side** search / filter / sort (do this in SQL, not in
Java after a `SELECT *`).

| Query param    | Example          | Notes                                                |
|----------------|------------------|------------------------------------------------------|
| `page`         | `2`              | default `1`                                          |
| `pageSize`     | `12`             | default `12`, max `100`                              |
| `search`       | `cart`           | matches name / description / tag                     |
| `status`       | `active,draft`   | comma-separated                                      |
| `dataSourceId` | `ds_001`         | segments fed by this source (`segment_data_sources`) |
| `tag`          | `high-value`     | single tag                                           |
| `sort`         | `-audienceSize`  | `name`, `audienceSize`, `updatedAt`, `matchRate` (prefix `-` = desc) |

```jsonc
{
  "data": [ { "id": "seg_0001", "name": "...", "status": "active", "audienceSize": 1840500,
              "matchRate": 0.62, "tags": ["high-value","loyalty"], "dataSourceIds": ["ds_001"],
              "createdBy": "Giulia B.", "createdAt": "...", "updatedAt": "..." } ],
  "pagination": { "page": 1, "pageSize": 12, "total": 36, "totalPages": 3 }
}
```

### `GET /api/segments/{id}`  *(segments db)*
Single segment (incl. its `tags` and `dataSourceIds`). `404` if missing.

### `GET /api/segments/{id}/trend?range=30`  *(segments db)*
Time series from `segment_trend`. `range` in days (7–180, default 30).
`{ "data": [ { "date": "2026-05-04", "audienceSize": 1055229, "matchedProfiles": 697678 } ] }`

### `GET /api/segments/{id}/activations`  *(segments + activations db)*
Verify the segment exists (segments db), then return its activations (activations db), each enriched with its `destination`. A cross-context composition done in your code.

### `POST /api/segments`  *(segments db)*
Create. Body: `{ "name", "description?", "status?", "dataSourceIds?", "tags?" }`.
- `name` required (3–80 chars). Validate; on failure return `400` with `error.details`.
- `201` → `{ "data": <segment> }`.

### `PATCH /api/segments/{id}`  *(segments db)*
Partial update of `name`, `description`, `status`, `tags`, `dataSourceIds`.
`200` → updated segment; `404` if missing; `400` on validation failure.

### `DELETE /api/segments/{id}`  *(segments db)*
`204` on success; `404` if missing. (Cascades to tags / data-source links / trend.)

### `GET /api/data-sources`  *(profiles db)*
`{ "data": [ { "id", "name", "type", "status", "profilesCount", "matchRate", "lastSyncAt" } ] }`

### `GET /api/destinations`  *(activations db)*
`{ "data": [ { "id", "name", "color" } ] }`

### `GET /api/activations?segmentId=&destinationId=`  *(activations db)*
List activations, each enriched with `destination` (and `segmentId`).

### `POST /api/activations`  *(segments + activations db)*
Activate a segment to a destination. Body `{ "segmentId", "destinationId" }`.
Validate that the segment exists (segments db) and the destination exists (activations db). `201` → new activation with `status: "syncing"`; `400` otherwise.

---

## OpenAPI
Annotate your resources with Swagger/OpenAPI annotations and expose the generated spec (e.g. `GET /openapi.json`). Serving Swagger UI is a nice bonus, not required.

## Scope
Implementing **health + overview + the segments read/list endpoints + one write path (create) end-to-end and cleanly** is worth more than a rushed pass over every endpoint. See ASSIGNMENT.md for priorities.
