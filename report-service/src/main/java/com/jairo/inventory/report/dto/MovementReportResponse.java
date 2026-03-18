package com.jairo.inventory.report.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MovementReportResponse(UUID productId,
                                     OffsetDateTime generatedAt,
                                     List<MovementItem> movements) {

    public record MovementItem(UUID movementId,
                               String movementType,
                               long quantity,
                               String reference,
                               String notes,
                               String performedBy,
                               OffsetDateTime createdAt) {
    }
}
