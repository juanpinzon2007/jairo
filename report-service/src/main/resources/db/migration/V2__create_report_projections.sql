CREATE TABLE reported_movements (
    movement_id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity BIGINT NOT NULL,
    reference VARCHAR(255) NOT NULL,
    notes VARCHAR(255) NOT NULL,
    performed_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_reported_movements_product_id_created_at
    ON reported_movements (product_id, created_at DESC);

CREATE TABLE reported_product_stocks (
    product_id UUID PRIMARY KEY,
    current_stock BIGINT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
