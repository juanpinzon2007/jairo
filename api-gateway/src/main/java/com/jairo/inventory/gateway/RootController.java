package com.jairo.inventory.gateway;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class RootController {

    @GetMapping("/")
    public Mono<Map<String, Object>> index() {
        return Mono.just(Map.of(
                "service", "api-gateway",
                "status", "UP",
                "message", "Servicio desplegado correctamente. Usa los endpoints /api/* para consumir la API.",
                "health", "/actuator/health",
                "routes", List.of(
                        "/api/auth/login",
                        "/api/users",
                        "/api/categories",
                        "/api/products",
                        "/api/movements",
                        "/api/stocks/{productId}",
                        "/api/reports/stock-summary"
                )
        ));
    }
}
