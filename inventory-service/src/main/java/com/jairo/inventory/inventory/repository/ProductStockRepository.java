package com.jairo.inventory.inventory.repository;

import com.jairo.inventory.inventory.domain.ProductStock;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {
}
