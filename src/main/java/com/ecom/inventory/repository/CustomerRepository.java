package com.ecom.inventory.repository;

import com.ecom.inventory.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findByCustomerNameContainingIgnoreCase(String keyword, Pageable pageable);
}
