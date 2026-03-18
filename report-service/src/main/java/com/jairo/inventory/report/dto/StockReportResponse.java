package com.jairo.inventory.report.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record StockReportResponse(OffsetDateTime generatedAt,
                                  long totalProducts,
                                  long totalUnitsInStock,
                                  List<StockReportItem> items) {
}
