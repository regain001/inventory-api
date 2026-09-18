package com.ecom.inventory.repository;

import com.ecom.inventory.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    boolean existsByPoNumberIgnoreCase(String documentNumber);

    boolean existsByPoNumberIgnoreCaseAndIdNot(
            String documentNumber,
            Long id
    );

    @Query(value = """
        SELECT document_number
        FROM purchase_order
        WHERE document_number LIKE CONCAT(:prefix, '%')
        ORDER BY document_number DESC
        LIMIT 1
        """, nativeQuery = true)
    String findLastDocumentNumberByPrefix(
            @Param("prefix") String prefix
    );
}
