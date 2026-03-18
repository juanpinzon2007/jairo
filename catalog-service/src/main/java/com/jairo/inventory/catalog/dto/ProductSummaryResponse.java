package com.jairo.inventory.catalog.dto;

import java.util.UUID;

public record ProductSummaryResponse(UUID id,
                                     String sku,
                                     String name) {
}
