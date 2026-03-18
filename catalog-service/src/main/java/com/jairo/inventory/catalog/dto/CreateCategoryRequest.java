package com.jairo.inventory.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank String name,
        @NotBlank String description
) {
}
