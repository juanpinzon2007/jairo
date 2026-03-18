package com.jairo.inventory.inventory.controller;

import com.jairo.inventory.shared.web.ApiError;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    ResponseEntity<ApiError> handleValidation(WebExchangeBindException ex, ServerWebExchange exchange) {
        Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (left, right) -> left));
        return build(HttpStatus.BAD_REQUEST, "Validation error", exchange.getRequest().getPath().value(), details);
    }

    @ExceptionHandler({AccessDeniedException.class, EntityNotFoundException.class, WebClientResponseException.class})
    ResponseEntity<ApiError> handleBusiness(Exception ex, ServerWebExchange exchange) {
        HttpStatus status = ex instanceof AccessDeniedException ? HttpStatus.BAD_REQUEST
                : ex instanceof EntityNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_GATEWAY;
        String message = ex instanceof WebClientResponseException
                ? "No se pudo validar el producto en catalog-service"
                : ex.getMessage();
        return build(status, message, exchange.getRequest().getPath().value(), Map.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, String path, Map<String, String> details) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, details));
    }
}
