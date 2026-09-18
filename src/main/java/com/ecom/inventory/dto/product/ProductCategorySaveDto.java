package com.ecom.inventory.dto.product;

import lombok.Data;

@Data
public class ProductCategorySaveDto {
    private Long id;             // null/absent = create
    private String categoryName;
    private String description;
    private Boolean active;
}
