CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE report_request_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_type VARCHAR(60) NOT NULL,
    requested_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
