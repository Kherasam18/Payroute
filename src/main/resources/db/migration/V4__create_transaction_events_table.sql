-- V4: Create transaction_events table
-- Audit log of all payment lifecycle events (initiated, success, failed, retry)

CREATE TABLE transaction_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    event_type VARCHAR(50) NOT NULL,
    payload JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index for querying events by transaction
CREATE INDEX idx_transaction_events_transaction_id ON transaction_events(transaction_id);
