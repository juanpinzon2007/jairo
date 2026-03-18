package com.jairo.inventory.report.client;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class CatalogReportClient {

    private final WebClient webClient;

    public CatalogReportClient(WebClient.Builder builder,
                               @Value("${clients.catalog.url:http://catalog-service:8082}") String catalogBaseUrl) {
        this.webClient = builder.baseUrl(catalogBaseUrl).build();
    }

    public Flux<ProductPayload> getProducts() {
        return webClient.get()
                .uri("/api/products")
                .retrieve()
                .bodyToFlux(ProductPayload.class);
    }

    public record ProductPayload(UUID id,
                                 String sku,
                                 String name,
                                 String description,
                                 BigDecimal unitPrice,
                                 UUID categoryId,
                                 String categoryName,
                                 boolean active,
                                 OffsetDateTime createdAt,
                                 long stock) {
    }
}
