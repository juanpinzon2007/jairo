package com.jairo.inventory.inventory.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StockResponse(UUID productId,
                            long currentStock,
                            OffsetDateTime updatedAt) {
}
