package com.jairo.inventory.report.controller;

import com.jairo.inventory.shared.web.ApiError;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    ResponseEntity<ApiError> handleWebClient(WebClientResponseException ex, ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError(
                        Instant.now(),
                        HttpStatus.BAD_GATEWAY.value(),
                        HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                        "No se pudo construir el reporte porque un servicio dependiente no respondio correctamente",
                        exchange.getRequest().getPath().value(),
                        Map.of()
                ));
    }
}
