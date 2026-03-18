package com.jairo.inventory.user.dto;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(UUID id,
                           String fullName,
                           String email,
                           boolean active,
                           Set<String> roles,
                           OffsetDateTime createdAt) {
}
