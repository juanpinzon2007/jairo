package com.jairo.inventory.gateway;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder, Environment environment) {
        String userUri = resolveServiceUri(environment, "SERVICES_USER_URI", "user-service");
        String catalogUri = resolveServiceUri(environment, "SERVICES_CATALOG_URI", "catalog-service");
        String inventoryUri = resolveServiceUri(environment, "SERVICES_INVENTORY_URI", "inventory-service");
        String reportUri = resolveServiceUri(environment, "SERVICES_REPORT_URI", "report-service");

        return builder.routes()
                .route("user-service", route -> route
                        .path("/api/auth/**", "/api/users/**")
                        .uri(userUri))
                .route("catalog-service", route -> route
                        .path("/api/categories/**", "/api/products/**")
                        .uri(catalogUri))
                .route("inventory-service", route -> route
                        .path("/api/movements/**", "/api/stocks/**")
                        .uri(inventoryUri))
                .route("report-service", route -> route
                        .path("/api/reports/**")
                        .uri(reportUri))
                .build();
    }

    private String resolveServiceUri(Environment environment, String variableName, String serviceName) {
        String configuredUri = environment.getProperty(variableName);
        if (StringUtils.hasText(configuredUri)) {
            return configuredUri;
        }

        if (isCloudProfileActive(environment)) {
            String derivedUri = deriveCloudRunUri(serviceName);
            if (StringUtils.hasText(derivedUri)) {
                return derivedUri;
            }
        }

        return "lb://" + serviceName;
    }

    private boolean isCloudProfileActive(Environment environment) {
        for (String profile : environment.getActiveProfiles()) {
            if ("cloud".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private String deriveCloudRunUri(String serviceName) {
        try {
            String projectNumber = readMetadata("http://metadata.google.internal/computeMetadata/v1/project/numeric-project-id");
            String regionPath = readMetadata("http://metadata.google.internal/computeMetadata/v1/instance/region");
            if (!StringUtils.hasText(projectNumber) || !StringUtils.hasText(regionPath)) {
                return null;
            }

            String[] regionParts = regionPath.split("/");
            String region = regionParts[regionParts.length - 1];
            return "https://" + serviceName + "-" + projectNumber + "." + region + ".run.app";
        } catch (IOException | InterruptedException ex) {
            return null;
        }
    }

    private String readMetadata(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(2))
                .header("Metadata-Flavor", "Google")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body().trim();
        }

        return null;
    }
}
