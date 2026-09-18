package com.ecom.inventory.repository;

import com.ecom.inventory.entity.InventoryLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLedgerReadRepository extends JpaRepository<InventoryLedgerEntry, Long> {
    long countByProductId(Long productId);
}
