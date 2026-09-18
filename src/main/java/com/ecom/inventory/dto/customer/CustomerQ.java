package com.ecom.inventory.dto.customer;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerQ {
    private Integer start;
    private Integer limit;
    private String keyword;
    private String customerType;
    private Boolean active;
}
