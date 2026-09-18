package com.ecom.inventory.repository;

import com.ecom.inventory.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    long countByCustomerId(Long id);
}
