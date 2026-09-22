package com.ecom.inventory.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_order")
@Getter
@Setter
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_number", nullable = false, unique = true, length = 30)
    private String documentNumber;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_type", nullable = false, length = 20)
    private String customerType;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @Column(name = "original_sales_order_id")
    private Long originalSalesOrderId;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "is_discount_applied", nullable = false)
    private boolean discountApplied;

    /** Resolved order-level discount; null = none. */
    @Column(name = "discount_amount")
    private Double discountAmount;

    /** Only set when the order discount was given as a percentage. */
    @Column(name = "discount_percentage")
    private Double discountPercentage;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "overall_status", nullable = false, length = 20)
    private String overallStatus;

    @Column(name = "note")
    private String note;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "contact_person_mobile_number", length = 20)
    private String contactPersonMobileNumber;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}