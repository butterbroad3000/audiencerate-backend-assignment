\connect activations

CREATE TABLE destinations (
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

INSERT INTO destinations (id, name, color) VALUES ('dest_001', 'Google Ads', '#4285F4');
INSERT INTO destinations (id, name, color) VALUES ('dest_002', 'Meta Ads', '#0866FF');
INSERT INTO destinations (id, name, color) VALUES ('dest_003', 'DV360', '#34A853');
INSERT INTO destinations (id, name, color) VALUES ('dest_004', 'The Trade Desk', '#1D4ED8');
INSERT INTO destinations (id, name, color) VALUES ('dest_005', 'Amazon DSP', '#FF9900');
INSERT INTO destinations (id, name, color) VALUES ('dest_006', 'LinkedIn Ads', '#0A66C2');
INSERT INTO destinations (id, name, color) VALUES ('dest_007', 'Adform', '#1A1A2E');

INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0001', 'seg_0004', 'dest_006', 'syncing', 2802144, now() - interval '9 days', now() - interval '16 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0002', 'seg_0004', 'dest_003', 'paused', 2072143, now() - interval '77 days', now() - interval '18 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0003', 'seg_0004', 'dest_005', 'live', 2563459, now() - interval '45 days', now() - interval '19 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0004', 'seg_0013', 'dest_001', 'paused', 148081, now() - interval '25 days', now() - interval '22 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0005', 'seg_0013', 'dest_005', 'error', 70570, now() - interval '6 days', now() - interval '41 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0006', 'seg_0014', 'dest_003', 'paused', 2583156, now() - interval '44 days', now() - interval '20 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0007', 'seg_0014', 'dest_005', 'live', 2155490, now() - interval '3 days', now() - interval '32 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0008', 'seg_0015', 'dest_005', 'paused', 1404782, now() - interval '23 days', now() - interval '6 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0009', 'seg_0015', 'dest_001', 'paused', 900630, now() - interval '6 days', now() - interval '29 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0010', 'seg_0017', 'dest_004', 'paused', 800522, now() - interval '2 days', now() - interval '42 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0011', 'seg_0017', 'dest_005', 'syncing', 1210022, now() - interval '73 days', now() - interval '39 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0012', 'seg_0019', 'dest_001', 'live', 257349, now() - interval '86 days', now() - interval '2 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0013', 'seg_0023', 'dest_005', 'live', 1967068, now() - interval '74 days', now() - interval '6 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0014', 'seg_0023', 'dest_004', 'error', 3299706, now() - interval '3 days', now() - interval '36 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0015', 'seg_0023', 'dest_007', 'paused', 2487744, now() - interval '81 days', now() - interval '2 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0016', 'seg_0027', 'dest_005', 'error', 1722483, now() - interval '43 days', now() - interval '6 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0017', 'seg_0027', 'dest_003', 'syncing', 1899627, now() - interval '33 days', now() - interval '37 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0018', 'seg_0029', 'dest_001', 'syncing', 1173663, now() - interval '15 days', now() - interval '29 hours');
INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at) VALUES ('act_0019', 'seg_0033', 'dest_005', 'paused', 1495155, now() - interval '33 days', now() - interval '12 hours');
