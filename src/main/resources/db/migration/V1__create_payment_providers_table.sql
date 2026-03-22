-- V1: Create payment_providers table
-- Stores payment provider configurations and their performance metrics

CREATE TABLE payment_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    success_rate DECIMAL(5, 2) NOT NULL DEFAULT 100.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
