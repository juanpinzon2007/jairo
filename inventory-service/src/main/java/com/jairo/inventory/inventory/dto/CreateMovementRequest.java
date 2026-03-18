package com.jairo.inventory.inventory.dto;

import com.jairo.inventory.inventory.domain.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateMovementRequest(
        @NotNull UUID productId,
        @NotNull MovementType movementType,
        @Min(1) long quantity,
        @NotBlank String reference,
        @NotBlank String notes
) {
}
