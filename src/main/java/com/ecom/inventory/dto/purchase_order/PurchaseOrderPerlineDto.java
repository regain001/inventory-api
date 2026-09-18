package com.ecom.inventory.dto.purchase_order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderPerlineDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double perlineTotalAmount;
}
