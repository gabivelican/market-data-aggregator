-- Insert seed data for symbols
INSERT INTO symbols (symbol_code, name, type, created_at) VALUES
('AAPL', 'Apple Inc.', 'STOCK', CURRENT_TIMESTAMP),
('BTC', 'Bitcoin', 'CRYPTOCURRENCY', CURRENT_TIMESTAMP),
('GOOGL', 'Alphabet Inc.', 'STOCK', CURRENT_TIMESTAMP)
ON CONFLICT (symbol_code) DO NOTHING;

-- Insert seed data for prices (historical data for AAPL)
INSERT INTO prices (symbol_id, price, volume, timestamp, created_at) VALUES
((SELECT id FROM symbols WHERE symbol_code = 'AAPL'), 150.25, 1000000, '2026-01-01 10:00:00', CURRENT_TIMESTAMP),
((SELECT id FROM symbols WHERE symbol_code = 'AAPL'), 151.50, 1200000, '2026-01-02 10:00:00', CURRENT_TIMESTAMP),
((SELECT id FROM symbols WHERE symbol_code = 'AAPL'), 149.75, 950000, '2026-01-03 10:00:00', CURRENT_TIMESTAMP);

-- Insert seed data for prices (historical data for BTC)
INSERT INTO prices (symbol_id, price, volume, timestamp, created_at) VALUES
((SELECT id FROM symbols WHERE symbol_code = 'BTC'), 42500.50, 500, '2026-01-01 10:00:00', CURRENT_TIMESTAMP),
((SELECT id FROM symbols WHERE symbol_code = 'BTC'), 43200.75, 480, '2026-01-02 10:00:00', CURRENT_TIMESTAMP),
((SELECT id FROM symbols WHERE symbol_code = 'BTC'), 41800.25, 520, '2026-01-03 10:00:00', CURRENT_TIMESTAMP);

-- Insert seed data for prices (historical data for GOOGL)
INSERT INTO prices (symbol_id, price, volume, timestamp, created_at) VALUES
((SELECT id FROM symbols WHERE symbol_code = 'GOOGL'), 140.30, 800000, '2026-01-01 10:00:00', CURRENT_TIMESTAMP),
((SELECT id FROM symbols WHERE symbol_code = 'GOOGL'), 141.80, 850000, '2026-01-02 10:00:00', CURRENT_TIMESTAMP),
((SELECT id FROM symbols WHERE symbol_code = 'GOOGL'), 139.50, 900000, '2026-01-03 10:00:00', CURRENT_TIMESTAMP);

-- Insert seed data for alerts
INSERT INTO alerts (symbol_id, alert_type, threshold, triggered_at, details, created_at) VALUES
((SELECT id FROM symbols WHERE symbol_code = 'AAPL'), 'PRICE_SPIKE', 150.00, '2026-01-02 10:00:00', 'Price exceeded threshold by 2%', CURRENT_TIMESTAMP),
((SELECT id FROM symbols WHERE symbol_code = 'BTC'), 'VOLUME_ANOMALY', 500, '2026-01-03 10:00:00', 'Unusual volume detected', CURRENT_TIMESTAMP);

-- Insert seed data for users
INSERT INTO users (username, password_hash, created_at) VALUES
('admin', 'hashed_password_123', CURRENT_TIMESTAMP),
('trader1', 'hashed_password_456', CURRENT_TIMESTAMP),
('analyst', 'hashed_password_789', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

