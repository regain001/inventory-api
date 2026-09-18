package com.ecom.inventory.dto.customer;

import lombok.Data;

@Data
public class CustomerSaveDto {
    private Long id;
    private String customerName;
    private String phone;
    private String address;
    private String customerType;
    private Boolean active;
}