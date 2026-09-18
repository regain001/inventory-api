package com.ecom.inventory.dto.customer;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerDto {
    private Long customerId;
    private String customerName;
    private String customerType;
    private String phone;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters / setters (or Lombok @Getter @Setter)
}
