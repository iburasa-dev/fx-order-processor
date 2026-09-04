-- ============================================================================
-- FX & Multi-Currency Order Processor - Database Initialization Script
-- Target Database: PostgreSQL 13+
-- ============================================================================

-- Step 1: Create Database (Run this separately if not yet created)
-- CREATE DATABASE fx_orders_db;
-- \c fx_orders_db;

-- Clean slate (optional)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS exchange_rate_snapshots CASCADE;

-- Orders
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    source_currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    source_subtotal NUMERIC(18, 4) NOT NULL,
    applied_exchange_rate NUMERIC(18, 6) NOT NULL,
    rate_source VARCHAR(32) NOT NULL,
    converted_subtotal NUMERIC(18, 2) NOT NULL,
    fee_percentage NUMERIC(6, 4) NOT NULL,
    fee_amount NUMERIC(18, 2) NOT NULL,
    net_total NUMERIC(18, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Order Items
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(18, 4) NOT NULL CHECK (unit_price > 0),
    line_total NUMERIC(18, 2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Exchange Rate Snapshots (for offline fallback)
CREATE TABLE exchange_rate_snapshots (
    id BIGSERIAL PRIMARY KEY,
    source_currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    rate NUMERIC(18, 6) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_rate_pair UNIQUE (source_currency, target_currency)
);

CREATE INDEX idx_exchange_rate_pair ON exchange_rate_snapshots(source_currency, target_currency);

-- Baseline exchange rates
INSERT INTO exchange_rate_snapshots (source_currency, target_currency, rate, updated_at)
VALUES 
    ('EUR', 'USD', 1.085000, CURRENT_TIMESTAMP),
    ('GBP', 'USD', 1.295000, CURRENT_TIMESTAMP),
    ('JPY', 'USD', 0.006700, CURRENT_TIMESTAMP),
    ('CAD', 'USD', 0.735000, CURRENT_TIMESTAMP),
    ('AUD', 'USD', 0.655000, CURRENT_TIMESTAMP),
    ('CHF', 'USD', 1.135000, CURRENT_TIMESTAMP),
    ('USD', 'EUR', 0.921600, CURRENT_TIMESTAMP),
    ('USD', 'GBP', 0.772200, CURRENT_TIMESTAMP),
    ('USD', 'JPY', 149.250000, CURRENT_TIMESTAMP),
    ('USD', 'AED', 3.672500, CURRENT_TIMESTAMP),
    ('AED', 'USD', 0.272294, CURRENT_TIMESTAMP),
    ('EUR', 'AED', 3.984663, CURRENT_TIMESTAMP),
    ('AED', 'EUR', 0.250962, CURRENT_TIMESTAMP),
    ('GBP', 'AED', 4.755888, CURRENT_TIMESTAMP),
    ('AED', 'GBP', 0.210266, CURRENT_TIMESTAMP),
    ('USD', 'SAR', 3.750000, CURRENT_TIMESTAMP),
    ('SAR', 'USD', 0.266667, CURRENT_TIMESTAMP),
    ('USD', 'INR', 83.950000, CURRENT_TIMESTAMP),
    ('INR', 'USD', 0.011912, CURRENT_TIMESTAMP)
ON CONFLICT (source_currency, target_currency) DO UPDATE 
SET rate = EXCLUDED.rate, updated_at = EXCLUDED.updated_at;
