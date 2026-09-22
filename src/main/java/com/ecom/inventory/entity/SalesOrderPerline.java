package com.ecom.inventory.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "sales_order_perline")
@Getter
@Setter
public class SalesOrderPerline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_order_id", nullable = false)
    private Long salesOrderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Column(name = "is_discount_applied", nullable = false)
    private boolean discountApplied;

    /** Discount PER UNIT. */
    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "discount_percentage")
    private Double discountPercentage;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "extended_price", nullable = false)
    private Double extendedPrice;

    @Column(name = "net_amount", nullable = false)
    private Double netAmount;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;
}