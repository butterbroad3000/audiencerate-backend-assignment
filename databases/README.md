# Database reference

A single PostgreSQL 16 server hosts **three databases**, one per bounded context.
They are seeded deterministically on first boot. Treat the schema as **given** — you shouldn't need to change it (if you add tables for your own mutations, document it).

Connect with `audiencerate` / `audiencerate` on `localhost:5432`.

## `profiles` — ingestion / identity context

**`data_sources`**

| column           | type          | notes                                  |
|------------------|---------------|----------------------------------------|
| `id`             | text PK       | e.g. `ds_001`                          |
| `name`           | text          |                                        |
| `type`           | text          | CRM, Website Pixel, Mobile SDK, …      |
| `status`         | text          | `connected` \| `error`                 |
| `profiles_count` | bigint        | number of profiles from this source    |
| `match_rate`     | numeric(4,3)  | 0..1                                   |
| `last_sync_at`   | timestamptz   |                                        |

## `segments` — audience context

**`segments`**

| column          | type          | notes                                   |
|-----------------|---------------|-----------------------------------------|
| `id`            | text PK       | e.g. `seg_0001`                         |
| `name`          | text          |                                         |
| `description`   | text          |                                         |
| `status`        | text          | `active` \| `draft` \| `archived`       |
| `audience_size` | bigint        |                                         |
| `match_rate`    | numeric(4,3)  | 0..1                                    |
| `created_by`    | text          |                                         |
| `created_at`    | timestamptz   |                                         |
| `updated_at`    | timestamptz   |                                         |

**`segment_tags`** — `(segment_id FK → segments, tag)`; a segment has many tags.

**`segment_data_sources`** — `(segment_id FK → segments, data_source_id)`.
`data_source_id` references `profiles.data_sources` in the **other database**, so there is intentionally **no foreign key** — joins across contexts happen in your application layer, not in SQL.

**`segment_trend`** — `(segment_id FK → segments, day DATE, audience_size, matched_profiles)`; 30 days of history per segment.

## `activations` — activation context

**`destinations`**

| column  | type    | notes                          |
|---------|---------|--------------------------------|
| `id`    | text PK | e.g. `dest_001`                |
| `name`  | text    | Google Ads, Meta Ads, …        |
| `color` | text    | hex                            |

**`activations`**

| column            | type          | notes                                          |
|-------------------|---------------|------------------------------------------------|
| `id`              | text PK       | e.g. `act_0001`                                |
| `segment_id`      | text          | references `segments.segments` (no cross-db FK) |
| `destination_id`  | text FK       | → `destinations`                               |
| `status`          | text          | `live` \| `syncing` \| `paused` \| `error`     |
| `synced_profiles` | bigint        |                                                |
| `created_at`      | timestamptz   |                                                |
| `last_sync_at`    | timestamptz   |                                                |

## Reset

```bash
docker compose down -v && docker compose up -d
```
