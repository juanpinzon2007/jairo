package com.jairo.inventory.inventory.client;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CatalogClient {

    private final WebClient webClient;

    public CatalogClient(WebClient.Builder builder,
                         @Value("${clients.catalog.url:http://catalog-service:8082}") String catalogBaseUrl) {
        this.webClient = builder.baseUrl(catalogBaseUrl).build();
    }

    public ProductSummary findProduct(UUID productId) {
        return webClient.get()
                .uri("/api/products/{productId}/summary", productId)
                .retrieve()
                .bodyToMono(ProductSummary.class)
                .block(Duration.ofSeconds(5));
    }

    public record ProductSummary(UUID id, String sku, String name) {
    }
}
