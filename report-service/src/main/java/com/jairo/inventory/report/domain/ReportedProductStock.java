package com.jairo.inventory.report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reported_product_stocks")
public class ReportedProductStock {

    @Id
    private UUID productId;

    @Column(nullable = false)
    private long currentStock;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public long getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(long currentStock) {
        this.currentStock = currentStock;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
