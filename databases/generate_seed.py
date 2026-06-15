#!/usr/bin/env python3
"""Generate deterministic seed SQL for the 3 DMP databases."""
import random
import os

random.seed(20240517)
OUT = os.path.join(os.path.dirname(__file__), "init")
os.makedirs(OUT, exist_ok=True)

def esc(s):
    return s.replace("'", "''")

# --- 00: create databases ---------------------------------------------------
with open(os.path.join(OUT, "00-create-databases.sql"), "w") as f:
    f.write(
        "-- Creates the three DMP databases. Runs against the default db on first boot.\n"
        "CREATE DATABASE profiles;\n"
        "CREATE DATABASE segments;\n"
        "CREATE DATABASE activations;\n"
    )

# --- 01: profiles db (identity / ingestion context) -------------------------
DS_TYPES = ["CRM", "Website Pixel", "Mobile SDK", "File Upload", "CDP", "Partner Feed"]
DS_NAMES = [
    "Salesforce CRM", "Web Pixel - Main Site", "iOS App SDK", "Android App SDK",
    "Loyalty Program Export", "Segment CDP", "Newsletter Platform", "Partner Data Feed",
]
data_sources = []
for i, name in enumerate(DS_NAMES, 1):
    data_sources.append({
        "id": f"ds_{i:03d}",
        "name": name,
        "type": random.choice(DS_TYPES),
        "status": "connected" if random.random() > 0.15 else "error",
        "profiles_count": random.randrange(120_000, 9_500_000, 1000),
        "match_rate": round(0.42 + random.random() * 0.5, 3),
        "last_sync_hours_ago": random.randint(1, 72),
    })

with open(os.path.join(OUT, "01-profiles.sql"), "w") as f:
    f.write("\\connect profiles\n\n")
    f.write("""CREATE TABLE data_sources (
    id             TEXT PRIMARY KEY,
    name           TEXT NOT NULL,
    type           TEXT NOT NULL,
    status         TEXT NOT NULL CHECK (status IN ('connected','error')),
    profiles_count BIGINT NOT NULL,
    match_rate     NUMERIC(4,3) NOT NULL,
    last_sync_at   TIMESTAMPTZ NOT NULL
);

""")
    for d in data_sources:
        f.write(
            "INSERT INTO data_sources (id, name, type, status, profiles_count, match_rate, last_sync_at) VALUES "
            f"('{d['id']}', '{esc(d['name'])}', '{d['type']}', '{d['status']}', "
            f"{d['profiles_count']}, {d['match_rate']}, now() - interval '{d['last_sync_hours_ago']} hours');\n"
        )
    f.write("\nCREATE INDEX idx_data_sources_status ON data_sources(status);\n")

# --- 02: segments db --------------------------------------------------------
TAG_POOL = ["in-market", "high-value", "lookalike", "retargeting", "b2b", "lapsed",
            "loyalty", "auto-intenders", "travel", "finance", "cart-abandoners", "newsletter"]
SEG_NAMES = [
    "High-Value Shoppers", "Cart Abandoners - 7 days", "Frequent Travellers", "Auto Intenders",
    "Lapsed Customers", "Newsletter Engaged", "Premium Card Holders", "Mobile App Power Users",
    "Black Friday Lookalike", "B2B Decision Makers", "Pet Owners", "Fitness Enthusiasts",
    "New Parents", "Home Improvement Intenders", "Streaming Subscribers", "Luxury Fashion Buyers",
    "Eco-Conscious Consumers", "Gaming Audience", "Recent Site Visitors", "Webinar Registrants",
    "Loyalty Tier Gold", "First-Time Buyers", "Cross-Sell Candidates", "Insurance Renewals",
    "Mortgage Intenders", "Frequent Flyers - EU", "Holiday Gift Shoppers", "High AOV - Last 90d",
    "Re-engagement Pool", "Email Openers - 30d", "In-Store + Online", "Smart Home Intenders",
    "Day-One Subscribers", "Win-back 90d", "Premium Trial Users", "Discount Seekers",
]
STATUS_WEIGHTS = ["active"] * 3 + ["draft"] * 2 + ["archived"]
DESCS = [
    "Profiles built from first-party behavioural signals.",
    "Deterministic match against CRM identities.",
    "Lookalike modelled on top converters.",
    "Cross-device audience resolved via the identity graph.",
    "Suppression list for active campaigns.",
    "High-intent users based on recent on-site activity.",
]
PEOPLE = ["Marco R.", "Giulia B.", "Alex T.", "Sara M.", "Davide P.", "Elena C."]

segments = []
for i, name in enumerate(SEG_NAMES, 1):
    seg = {
        "id": f"seg_{i:04d}",
        "name": name,
        "description": random.choice(DESCS),
        "status": random.choice(STATUS_WEIGHTS),
        "audience_size": random.randrange(8_000, 4_200_000, 500),
        "match_rate": round(0.38 + random.random() * 0.55, 3),
        "created_by": random.choice(PEOPLE),
        "created_days_ago": random.randint(5, 400),
        "tags": sorted(set(random.sample(TAG_POOL, k=2))),
        "ds": sorted(set(d["id"] for d in data_sources if random.random() > 0.6)) or [data_sources[0]["id"]],
    }
    seg["updated_days_ago"] = random.randint(0, min(seg["created_days_ago"], 30))
    segments.append(seg)

with open(os.path.join(OUT, "02-segments.sql"), "w") as f:
    f.write("\\connect segments\n\n")
    f.write("CREATE SEQUENCE IF NOT EXISTS segments_id_seq START 37;\n\n")
    f.write("""CREATE TABLE segments (
    id            TEXT PRIMARY KEY,
    name          TEXT NOT NULL,
    description   TEXT,
    status        TEXT NOT NULL CHECK (status IN ('active','draft','archived')),
    audience_size BIGINT NOT NULL,
    match_rate    NUMERIC(4,3) NOT NULL,
    created_by    TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE segment_tags (
    segment_id TEXT NOT NULL REFERENCES segments(id) ON DELETE CASCADE,
    tag        TEXT NOT NULL,
    PRIMARY KEY (segment_id, tag)
);

-- Cross-context reference: data_source_id lives in the `profiles` database,
-- so there is intentionally no foreign key here.
CREATE TABLE segment_data_sources (
    segment_id     TEXT NOT NULL REFERENCES segments(id) ON DELETE CASCADE,
    data_source_id TEXT NOT NULL,
    PRIMARY KEY (segment_id, data_source_id)
);

CREATE TABLE segment_trend (
    segment_id       TEXT NOT NULL REFERENCES segments(id) ON DELETE CASCADE,
    day              DATE NOT NULL,
    audience_size    BIGINT NOT NULL,
    matched_profiles BIGINT NOT NULL,
    PRIMARY KEY (segment_id, day)
);

CREATE INDEX idx_segments_status ON segments(status);
CREATE INDEX idx_segments_updated_at ON segments(updated_at);

""")
    for s in segments:
        f.write(
            "INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES "
            f"('{s['id']}', '{esc(s['name'])}', '{esc(s['description'])}', '{s['status']}', "
            f"{s['audience_size']}, {s['match_rate']}, '{s['created_by']}', "
            f"now() - interval '{s['created_days_ago']} days', now() - interval '{s['updated_days_ago']} days');\n"
        )
    f.write("\n")
    for s in segments:
        for t in s["tags"]:
            f.write(f"INSERT INTO segment_tags (segment_id, tag) VALUES ('{s['id']}', '{t}');\n")
    f.write("\n")
    for s in segments:
        for ds in s["ds"]:
            f.write(f"INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('{s['id']}', '{ds}');\n")
    f.write("\n-- 30 days of trend data for every segment, synthesised procedurally\n")
    f.write("""INSERT INTO segment_trend (segment_id, day, audience_size, matched_profiles)
SELECT s.id,
       g.day::date,
       GREATEST(1000, (s.audience_size * (0.78 + 0.22 * random()))::bigint) AS audience_size,
       GREATEST(500,  (s.audience_size * s.match_rate * (0.78 + 0.22 * random()))::bigint) AS matched_profiles
FROM segments s
CROSS JOIN generate_series(current_date - interval '29 days', current_date, interval '1 day') AS g(day);
""")

# --- 03: activations db -----------------------------------------------------
DESTS = [
    ("dest_001", "Google Ads", "#4285F4"), ("dest_002", "Meta Ads", "#0866FF"),
    ("dest_003", "DV360", "#34A853"), ("dest_004", "The Trade Desk", "#1D4ED8"),
    ("dest_005", "Amazon DSP", "#FF9900"), ("dest_006", "LinkedIn Ads", "#0A66C2"),
    ("dest_007", "Adform", "#1A1A2E"),
]
ACT_STATUS = ["live", "live", "syncing", "paused", "error"]
activations = []
aseq = 0
for s in segments:
    if s["status"] != "active":
        continue
    used = set()
    for _ in range(random.randint(0, 3)):
        dest = random.choice(DESTS)
        if dest[0] in used:
            continue
        used.add(dest[0])
        aseq += 1
        activations.append({
            "id": f"act_{aseq:04d}",
            "segment_id": s["id"],
            "destination_id": dest[0],
            "status": random.choice(ACT_STATUS),
            "synced": round(s["audience_size"] * (0.4 + random.random() * 0.5)),
            "created_days_ago": random.randint(1, 90),
            "sync_hours_ago": random.randint(1, 48),
        })

with open(os.path.join(OUT, "03-activations.sql"), "w") as f:
    f.write("\\connect activations\n\n")
    f.write("CREATE SEQUENCE IF NOT EXISTS activations_id_seq START 20;\n\n")
    f.write("""CREATE TABLE destinations (
    id    TEXT PRIMARY KEY,
    name  TEXT NOT NULL,
    color TEXT NOT NULL
);

-- Cross-context reference: segment_id lives in the `segments` database,
-- so there is intentionally no foreign key here.
CREATE TABLE activations (
    id              TEXT PRIMARY KEY,
    segment_id      TEXT NOT NULL,
    destination_id  TEXT NOT NULL REFERENCES destinations(id),
    status          TEXT NOT NULL CHECK (status IN ('live','syncing','paused','error')),
    synced_profiles BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_sync_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activations_segment ON activations(segment_id);
CREATE INDEX idx_activations_destination ON activations(destination_id);

""")
    for d in DESTS:
        f.write(f"INSERT INTO destinations (id, name, color) VALUES ('{d[0]}', '{esc(d[1])}', '{d[2]}');\n")
    f.write("\n")
    for a in activations:
        f.write(
            "INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES "
            f"('{a['id']}', '{a['segment_id']}', '{a['destination_id']}', '{a['status']}', "
            f"{a['synced']}, now() - interval '{a['created_days_ago']} days', now() - interval '{a['sync_hours_ago']} hours');\n"
        )

print(f"data_sources={len(data_sources)} segments={len(segments)} destinations={len(DESTS)} activations={len(activations)}")
