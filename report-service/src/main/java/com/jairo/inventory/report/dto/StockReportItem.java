package com.jairo.inventory.report.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockReportItem(UUID productId,
                              String sku,
                              String productName,
                              String categoryName,
                              BigDecimal unitPrice,
                              long stock) {
}
