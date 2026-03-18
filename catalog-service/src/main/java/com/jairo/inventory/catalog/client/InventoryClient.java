package com.jairo.inventory.catalog.client;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class InventoryClient {

    private final WebClient webClient;

    public InventoryClient(WebClient.Builder builder,
                           @Value("${clients.inventory.url:http://inventory-service:8083}") String inventoryBaseUrl) {
        this.webClient = builder.baseUrl(inventoryBaseUrl).build();
    }

    public ProductStockResponse getStock(UUID productId) {
        return webClient.get()
                .uri("/api/stocks/{productId}", productId)
                .retrieve()
                .bodyToMono(ProductStockResponse.class)
                .block(Duration.ofSeconds(5));
    }

    public record ProductStockResponse(UUID productId, long currentStock) {
    }
}
