package com.ecom.inventory.dto.sales_order;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalesOrderDto {
    private Long salesOrderId;
    private String documentNumber;
    private String reference;
    private LocalDate orderDate;
    private Long customerId;
    private String customerType;
    private String orderType;
    private Long originalSalesOrderId;
    private Integer totalQuantity;
    private Double discountAmount;
    private Double discountPercentage;
    private Double totalAmount;
    private String overallStatus;
    private String note;
    private String deliveryAddress;
    private String contactPersonMobileNumber;
    private Long createdBy;
    private LocalDateTime createdAt;
    private List<SalesOrderPerlineDto> perlines;
}
