-- Create users table
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,           -- MODIFICAT: SERIAL -> BIGSERIAL
                                     username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Create symbols table
CREATE TABLE IF NOT EXISTS symbols (
                                       id BIGSERIAL PRIMARY KEY,           -- MODIFICAT: SERIAL -> BIGSERIAL
                                       symbol_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Create index on symbol_code for faster lookups
CREATE INDEX idx_symbols_symbol_code ON symbols(symbol_code);

-- Create prices table
CREATE TABLE IF NOT EXISTS prices (
                                      id BIGSERIAL PRIMARY KEY,           -- MODIFICAT: SERIAL -> BIGSERIAL
                                      symbol_id BIGINT NOT NULL,          -- MODIFICAT: INTEGER -> BIGINT (ca sa se potriveasca cu symbols.id)
                                      price DECIMAL(18, 8) NOT NULL,
    volume BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prices_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE
    );

-- Create index on symbol_id and timestamp for efficient queries
CREATE INDEX idx_prices_symbol_id ON prices(symbol_id);
CREATE INDEX idx_prices_timestamp ON prices(timestamp);
CREATE INDEX idx_prices_symbol_timestamp ON prices(symbol_id, timestamp);

-- Create alerts table
CREATE TABLE IF NOT EXISTS alerts (
                                      id BIGSERIAL PRIMARY KEY,           -- MODIFICAT: SERIAL -> BIGSERIAL
                                      symbol_id BIGINT NOT NULL,          -- MODIFICAT: INTEGER -> BIGINT (ca sa se potriveasca cu symbols.id)
                                      alert_type VARCHAR(50) NOT NULL,
    threshold DECIMAL(18, 8) NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alerts_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE
    );

-- Create index on symbol_id and triggered_at
CREATE INDEX idx_alerts_symbol_id ON alerts(symbol_id);
CREATE INDEX idx_alerts_triggered_at ON alerts(triggered_at);