package com.ecom.inventory.repository;

import com.ecom.inventory.entity.SalesOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    long countByCustomerId(Long id);

    /** Row lock: serialises edit / complete / delete on the same order. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SalesOrder s where s.id = :id")
    Optional<SalesOrder> findByIdForUpdate(@Param("id") Long id);

    /** Replaces the old FK guard: is this order the original of any return? */
    boolean existsByOriginalSalesOrderId(Long originalSalesOrderId);

    /** Atomic, race-free counter per prefix (e.g. 'SO-2026'). Rolls back with the transaction. */
    @Query(value = """
            INSERT INTO sales_order_counter (prefix, last_value) VALUES (:prefix, 1)
            ON CONFLICT (prefix) DO UPDATE SET last_value = sales_order_counter.last_value + 1
            RETURNING last_value
            """, nativeQuery = true)
    Long nextDocumentSequence(@Param("prefix") String prefix);
}
