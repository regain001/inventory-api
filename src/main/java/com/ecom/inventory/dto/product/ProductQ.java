package com.ecom.inventory.dto.product;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductQ {
    private Integer start;
    private Integer limit;
    private String keyword;
    private Long categoryId;
    private Boolean active;
    private Double minPrice;
    private Double maxPrice;

    // getters and setters
}