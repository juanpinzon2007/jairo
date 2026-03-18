package com.jairo.inventory.shared.security;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(UUID userId,
                                String fullName,
                                String email,
                                List<String> roles) {
}
