package com.ecom.inventory.dto.sales_order;

import lombok.Data;

@Data
public class SalesOrderLineSaveDto {
    private Long productId;
    private Integer quantity;
    private Double discountAmount;        // PER UNIT flat amount, optional (send this OR discountPercentage)
    private Double discountPercentage;    // % of base price, optional
}
