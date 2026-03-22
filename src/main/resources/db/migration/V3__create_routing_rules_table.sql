-- V3: Create routing_rules table
-- Defines amount/currency-based routing rules to preferred providers

CREATE TABLE routing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    min_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    max_amount DECIMAL(19, 4) NOT NULL DEFAULT 999999999.9999,
    currency VARCHAR(3) NOT NULL,
    preferred_provider_id UUID REFERENCES payment_providers(id),
    priority INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
