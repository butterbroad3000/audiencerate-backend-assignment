\connect segments

CREATE SEQUENCE IF NOT EXISTS segments_id_seq START 37;

CREATE TABLE segments (
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

INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0001', 'High-Value Shoppers', 'Deterministic match against CRM identities.', 'active', 1284500, 0.561, 'Sara M.', now() - interval '380 days', now() - interval '10 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0002', 'Cart Abandoners - 7 days', 'High-intent users based on recent on-site activity.', 'draft', 3552500, 0.597, 'Sara M.', now() - interval '311 days', now() - interval '21 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0003', 'Frequent Travellers', 'Cross-device audience resolved via the identity graph.', 'draft', 2503000, 0.566, 'Sara M.', now() - interval '374 days', now() - interval '23 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0004', 'Auto Intenders', 'Suppression list for active campaigns.', 'active', 4018500, 0.774, 'Marco R.', now() - interval '67 days', now() - interval '0 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0005', 'Lapsed Customers', 'Lookalike modelled on top converters.', 'archived', 815000, 0.499, 'Giulia B.', now() - interval '261 days', now() - interval '20 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0006', 'Newsletter Engaged', 'Lookalike modelled on top converters.', 'archived', 73500, 0.661, 'Davide P.', now() - interval '70 days', now() - interval '21 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0007', 'Premium Card Holders', 'High-intent users based on recent on-site activity.', 'draft', 661000, 0.699, 'Giulia B.', now() - interval '306 days', now() - interval '2 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0008', 'Mobile App Power Users', 'High-intent users based on recent on-site activity.', 'archived', 4068000, 0.705, 'Marco R.', now() - interval '288 days', now() - interval '3 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0009', 'Black Friday Lookalike', 'Deterministic match against CRM identities.', 'draft', 1442500, 0.926, 'Elena C.', now() - interval '161 days', now() - interval '3 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0010', 'B2B Decision Makers', 'High-intent users based on recent on-site activity.', 'active', 3316000, 0.79, 'Elena C.', now() - interval '254 days', now() - interval '23 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0011', 'Pet Owners', 'High-intent users based on recent on-site activity.', 'archived', 2966500, 0.639, 'Elena C.', now() - interval '94 days', now() - interval '30 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0012', 'Fitness Enthusiasts', 'Cross-device audience resolved via the identity graph.', 'archived', 1127500, 0.432, 'Elena C.', now() - interval '130 days', now() - interval '25 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0013', 'New Parents', 'Profiles built from first-party behavioural signals.', 'active', 174500, 0.772, 'Davide P.', now() - interval '151 days', now() - interval '10 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0014', 'Home Improvement Intenders', 'Deterministic match against CRM identities.', 'active', 3323500, 0.643, 'Marco R.', now() - interval '106 days', now() - interval '15 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0015', 'Streaming Subscribers', 'Cross-device audience resolved via the identity graph.', 'active', 1704500, 0.844, 'Elena C.', now() - interval '308 days', now() - interval '10 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0016', 'Luxury Fashion Buyers', 'Profiles built from first-party behavioural signals.', 'draft', 2135500, 0.409, 'Marco R.', now() - interval '363 days', now() - interval '25 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0017', 'Eco-Conscious Consumers', 'Cross-device audience resolved via the identity graph.', 'active', 1394500, 0.872, 'Elena C.', now() - interval '396 days', now() - interval '28 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0018', 'Gaming Audience', 'High-intent users based on recent on-site activity.', 'active', 333500, 0.821, 'Alex T.', now() - interval '313 days', now() - interval '27 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0019', 'Recent Site Visitors', 'Cross-device audience resolved via the identity graph.', 'active', 485000, 0.898, 'Sara M.', now() - interval '303 days', now() - interval '0 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0020', 'Webinar Registrants', 'Cross-device audience resolved via the identity graph.', 'draft', 3739000, 0.914, 'Elena C.', now() - interval '279 days', now() - interval '17 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0021', 'Loyalty Tier Gold', 'Cross-device audience resolved via the identity graph.', 'draft', 3634500, 0.39, 'Giulia B.', now() - interval '246 days', now() - interval '22 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0022', 'First-Time Buyers', 'Suppression list for active campaigns.', 'archived', 25000, 0.789, 'Davide P.', now() - interval '40 days', now() - interval '14 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0023', 'Cross-Sell Candidates', 'High-intent users based on recent on-site activity.', 'active', 3746500, 0.423, 'Sara M.', now() - interval '178 days', now() - interval '14 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0024', 'Insurance Renewals', 'Cross-device audience resolved via the identity graph.', 'archived', 511000, 0.624, 'Alex T.', now() - interval '279 days', now() - interval '22 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0025', 'Mortgage Intenders', 'Lookalike modelled on top converters.', 'draft', 1202000, 0.84, 'Elena C.', now() - interval '38 days', now() - interval '27 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0026', 'Frequent Flyers - EU', 'Suppression list for active campaigns.', 'archived', 2786000, 0.71, 'Marco R.', now() - interval '94 days', now() - interval '10 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0027', 'Holiday Gift Shoppers', 'High-intent users based on recent on-site activity.', 'active', 2338000, 0.845, 'Giulia B.', now() - interval '193 days', now() - interval '17 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0028', 'High AOV - Last 90d', 'Profiles built from first-party behavioural signals.', 'archived', 1923500, 0.814, 'Giulia B.', now() - interval '246 days', now() - interval '23 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0029', 'Re-engagement Pool', 'Lookalike modelled on top converters.', 'active', 2296500, 0.811, 'Davide P.', now() - interval '140 days', now() - interval '24 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0030', 'Email Openers - 30d', 'High-intent users based on recent on-site activity.', 'draft', 893500, 0.887, 'Davide P.', now() - interval '167 days', now() - interval '29 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0031', 'In-Store + Online', 'Deterministic match against CRM identities.', 'draft', 1536000, 0.762, 'Alex T.', now() - interval '38 days', now() - interval '10 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0032', 'Smart Home Intenders', 'Lookalike modelled on top converters.', 'archived', 3537500, 0.507, 'Marco R.', now() - interval '169 days', now() - interval '25 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0033', 'Day-One Subscribers', 'High-intent users based on recent on-site activity.', 'active', 2871000, 0.875, 'Giulia B.', now() - interval '149 days', now() - interval '19 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0034', 'Win-back 90d', 'Lookalike modelled on top converters.', 'draft', 847500, 0.62, 'Marco R.', now() - interval '275 days', now() - interval '4 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0035', 'Premium Trial Users', 'High-intent users based on recent on-site activity.', 'active', 1803500, 0.673, 'Davide P.', now() - interval '101 days', now() - interval '15 days');
INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at) VALUES ('seg_0036', 'Discount Seekers', 'Deterministic match against CRM identities.', 'archived', 3786000, 0.656, 'Giulia B.', now() - interval '211 days', now() - interval '14 days');

INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0001', 'in-market');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0001', 'travel');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0002', 'lookalike');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0002', 'travel');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0003', 'cart-abandoners');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0003', 'retargeting');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0004', 'cart-abandoners');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0004', 'lookalike');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0005', 'lapsed');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0005', 'travel');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0006', 'cart-abandoners');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0006', 'high-value');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0007', 'high-value');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0007', 'retargeting');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0008', 'in-market');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0008', 'lookalike');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0009', 'b2b');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0009', 'loyalty');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0010', 'high-value');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0010', 'lookalike');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0011', 'lapsed');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0011', 'newsletter');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0012', 'b2b');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0012', 'retargeting');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0013', 'high-value');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0013', 'travel');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0014', 'in-market');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0014', 'newsletter');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0015', 'cart-abandoners');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0015', 'travel');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0016', 'in-market');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0016', 'lapsed');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0017', 'auto-intenders');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0017', 'in-market');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0018', 'finance');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0018', 'lookalike');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0019', 'auto-intenders');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0019', 'in-market');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0020', 'auto-intenders');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0020', 'retargeting');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0021', 'in-market');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0021', 'travel');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0022', 'b2b');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0022', 'finance');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0023', 'b2b');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0023', 'in-market');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0024', 'b2b');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0024', 'loyalty');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0025', 'high-value');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0025', 'lapsed');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0026', 'b2b');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0026', 'high-value');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0027', 'lapsed');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0027', 'travel');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0028', 'cart-abandoners');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0028', 'lapsed');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0029', 'auto-intenders');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0029', 'loyalty');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0030', 'lookalike');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0030', 'retargeting');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0031', 'finance');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0031', 'retargeting');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0032', 'b2b');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0032', 'travel');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0033', 'finance');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0033', 'travel');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0034', 'cart-abandoners');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0034', 'finance');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0035', 'lapsed');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0035', 'retargeting');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0036', 'cart-abandoners');
INSERT INTO segment_tags (segment_id, tag) VALUES ('seg_0036', 'in-market');

INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0001', 'ds_004');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0001', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0002', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0002', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0002', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0002', 'ds_004');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0002', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0003', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0003', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0003', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0003', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0004', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0004', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0004', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0005', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0006', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0006', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0007', 'ds_004');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0007', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0007', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0008', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0008', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0008', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0009', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0010', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0010', 'ds_004');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0010', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0011', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0011', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0012', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0013', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0013', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0013', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0013', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0014', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0014', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0014', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0014', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0014', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0015', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0015', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0015', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0015', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0016', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0016', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0016', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0016', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0017', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0017', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0017', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0017', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0018', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0019', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0019', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0019', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0020', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0020', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0020', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0020', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0021', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0021', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0021', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0021', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0022', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0022', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0022', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0023', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0023', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0024', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0024', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0024', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0025', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0026', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0027', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0027', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0028', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0028', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0028', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0029', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0029', 'ds_004');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0029', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0029', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0029', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0030', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0030', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0030', 'ds_004');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0030', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0030', 'ds_008');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0031', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0031', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0031', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0031', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0032', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0032', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0032', 'ds_004');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0032', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0032', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0032', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0033', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0033', 'ds_007');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0034', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0034', 'ds_004');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0034', 'ds_005');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0035', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0035', 'ds_003');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0035', 'ds_004');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0035', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0036', 'ds_001');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0036', 'ds_002');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0036', 'ds_006');
INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES ('seg_0036', 'ds_007');

-- 30 days of trend data for every segment, synthesised procedurally
INSERT INTO segment_trend (segment_id, day, audience_size, matched_profiles)
SELECT s.id,
       g.day::date,
       GREATEST(1000, (s.audience_size * (0.78 + 0.22 * random()))::bigint) AS audience_size,
       GREATEST(500,  (s.audience_size * s.match_rate * (0.78 + 0.22 * random()))::bigint) AS matched_profiles
FROM segments s
CROSS JOIN generate_series(current_date - interval '29 days', current_date, interval '1 day') AS g(day);
