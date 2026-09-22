package com.ecom.inventory.dto.sales_order;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/** Request body for create / update. Status is NOT accepted here; use POST /{id}/complete. */
@Data
public class SalesOrderSaveDto {
    private Long salesOrderId;                 // set from path on update
    private String reference;
    private LocalDate orderDate;
    private Long customerId;
    private String customerType;              // default 'PRAN RFL'
    private String orderType;                 // default 'SALE'; RETURN requires originalSalesOrderId
    private Long originalSalesOrderId;
    private Double discountAmount;        // order-level flat amount, optional (send this OR discountPercentage)
    private Double discountPercentage;    // order-level % of the subtotal, optional
    private String note;
    private String deliveryAddress;
    private String contactPersonMobileNumber;
    private List<SalesOrderLineSaveDto> lines;
}
