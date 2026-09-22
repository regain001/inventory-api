package com.ecom.inventory.dto.sales_order;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Query params for GET /api/sales-orders */
@Data
public class SalesOrderQ {
    private String keyword;          // document_number / reference
    private String overallStatus;    // Pending | Completed
    private String orderType;        // SALE | RETURN
    private Long customerId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    private Integer start;
    private Integer limit;
}
