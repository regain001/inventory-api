package com.ecom.inventory.dto.purchase_order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PurchaseOrderSaveDto {
    private Long purchaseOrderId;
    private String documentNumber;
    private String poNumber;
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Asia/Dhaka")
    private String transactionDate;
    private String note;
    private String overallStatus;
    private Long createdBy;
    private List<PurchaseOrderPerlineDto> lines;
}
