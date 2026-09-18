package com.ecom.inventory.dto.product;

import lombok.Data;

@Data
public class ProductSaveDto {
    private Long id;
    private String sku;
    private String productName;
    private Long categoryId;
    private Double price;
    private Integer packQuantity;
    private String packUnit;
    private Integer minStockLevel;
    private Boolean active;
}
