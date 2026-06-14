\connect profiles

CREATE TABLE data_sources (
    id             TEXT PRIMARY KEY,
    name           TEXT NOT NULL,
    type           TEXT NOT NULL,
    status         TEXT NOT NULL CHECK (status IN ('connected','error')),
    profiles_count BIGINT NOT NULL,
    match_rate     NUMERIC(4,3) NOT NULL,
    last_sync_at   TIMESTAMPTZ NOT NULL
);

INSERT INTO data_sources (id, name, type, status, profiles_count, match_rate, last_sync_at) VALUES ('ds_001', 'Salesforce CRM', 'Website Pixel', 'error', 1504000, 0.471, now() - interval '53 hours');
INSERT INTO data_sources (id, name, type, status, profiles_count, match_rate, last_sync_at) VALUES ('ds_002', 'Web Pixel - Main Site', 'Partner Feed', 'connected', 3246000, 0.892, now() - interval '62 hours');
INSERT INTO data_sources (id, name, type, status, profiles_count, match_rate, last_sync_at) VALUES ('ds_003', 'iOS App SDK', 'CRM', 'connected', 3380000, 0.634, now() - interval '68 hours');
INSERT INTO data_sources (id, name, type, status, profiles_count, match_rate, last_sync_at) VALUES ('ds_004', 'Android App SDK', 'Website Pixel', 'connected', 6689000, 0.83, now() - interval '70 hours');
INSERT INTO data_sources (id, name, type, status, profiles_count, match_rate, last_sync_at) VALUES ('ds_005', 'Loyalty Program Export', 'CRM', 'connected', 5421000, 0.764, now() - interval '55 hours');
INSERT INTO data_sources (id, name, type, status, profiles_count, match_rate, last_sync_at) VALUES ('ds_006', 'Segment CDP', 'CDP', 'connected', 7094000, 0.596, now() - interval '67 hours');
INSERT INTO data_sources (id, name, type, status, profiles_count, match_rate, last_sync_at) VALUES ('ds_007', 'Newsletter Platform', 'Partner Feed', 'connected', 960000, 0.903, now() - interval '32 hours');
INSERT INTO data_sources (id, name, type, status, profiles_count, match_rate, last_sync_at) VALUES ('ds_008', 'Partner Data Feed', 'Website Pixel', 'connected', 719000, 0.762, now() - interval '3 hours');

CREATE INDEX idx_data_sources_status ON data_sources(status);
