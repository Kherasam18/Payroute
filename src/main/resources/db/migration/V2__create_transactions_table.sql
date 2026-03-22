-- V2: Create transactions table
-- Core transaction record with provider FK, idempotency key, and JSONB payloads

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    provider_id UUID REFERENCES payment_providers(id),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    request_payload JSONB,
    response_payload JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for frequent query patterns
CREATE INDEX idx_transactions_idempotency_key ON transactions(idempotency_key);
CREATE INDEX idx_transactions_status ON transactions(status);
