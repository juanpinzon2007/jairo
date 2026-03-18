package com.jairo.inventory.shared.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryMovementEvent(UUID movementId,
                                     UUID productId,
                                     String movementType,
                                     long quantity,
                                     String reference,
                                     String notes,
                                     String performedBy,
                                     OffsetDateTime createdAt,
                                     long currentStock) {
}
