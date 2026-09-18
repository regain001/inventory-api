package com.ecom.inventory.dto.purchase_order;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PurchaseOrderQ {
    private Integer start;
    private Integer limit;
    private String keyword;
    private String overallStatus;
    private LocalDate fromDate;
    private LocalDate toDate;
}
