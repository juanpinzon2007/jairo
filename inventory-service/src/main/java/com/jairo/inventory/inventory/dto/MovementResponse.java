package com.jairo.inventory.inventory.dto;

import com.jairo.inventory.inventory.domain.MovementType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MovementResponse(UUID id,
                               UUID productId,
                               MovementType movementType,
                               long quantity,
                               String reference,
                               String notes,
                               String performedBy,
                               OffsetDateTime createdAt) {
}
