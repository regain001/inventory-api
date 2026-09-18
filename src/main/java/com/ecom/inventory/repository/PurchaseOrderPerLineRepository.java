package com.ecom.inventory.repository;

import com.ecom.inventory.entity.PurchaseOrderPerline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderPerLineRepository
        extends JpaRepository<PurchaseOrderPerline, Long> {

    List<PurchaseOrderPerline> findByPurchaseOrderId(Long purchaseOrderId);

    long countByPurchaseOrderId(Long purchaseOrderId);

    void deleteByPurchaseOrderId(Long purchaseOrderId);
}
