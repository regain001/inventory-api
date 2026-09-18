package com.ecom.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "current_stock")
@Getter
@Setter
public class CurrentStock {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "last_stock_in_at")
    private Timestamp lastStockInAt;

    @Column(name = "last_stock_out_at")
    private LocalDateTime lastStockOutAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
