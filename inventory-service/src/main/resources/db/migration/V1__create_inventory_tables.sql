CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE product_stocks (
    product_id UUID PRIMARY KEY,
    current_stock BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity BIGINT NOT NULL,
    reference VARCHAR(120) NOT NULL,
    notes VARCHAR(255) NOT NULL,
    performed_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inventory_movements_product_id ON inventory_movements (product_id);
