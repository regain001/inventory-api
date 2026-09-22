package com.ecom.inventory.dto.sales_order;


import lombok.Data;

@Data
public class SalesOrderPerlineDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Integer quantity;
    private String unitOfMeasure;
    private Double basePrice;
    private Double discountAmount;        // per unit
    private Double discountPercentage;
    private Double unitPrice;
    private Double extendedPrice;
    private Double netAmount;
}
