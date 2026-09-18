package com.ecom.inventory.dto.purchase_order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderDto {
    private Long purchaseOrderId;
    private String poNumber;
    private String documentNumber;
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Asia/Dhaka")
    private LocalDate transactionDate;
    private String note;
    private Integer totalQuantity;
    private Double totalAmount;
    private String overallStatus;
    private Long createdBy;
    private LocalDateTime createdAt;
    private List<PurchaseOrderPerlineDto> perlines;
}
