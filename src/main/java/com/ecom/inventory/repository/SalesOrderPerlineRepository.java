package com.ecom.inventory.repository;

import com.ecom.inventory.entity.SalesOrderPerline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalesOrderPerlineRepository extends JpaRepository<SalesOrderPerline, Long> {

    List<SalesOrderPerline> findBySalesOrderIdOrderById(Long salesOrderId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from SalesOrderPerline l where l.salesOrderId = :id")
    void deleteBySalesOrderId(@Param("id") Long id);

    /** Quantity already returned per product against one original sale. Rows: [product_id, sum(quantity)]. */
    @Query(value = """
            SELECT sol.product_id, SUM(sol.quantity)
            FROM sales_order_perline sol
            JOIN sales_order so ON so.id = sol.sales_order_id
            WHERE so.original_sales_order_id = :originalId
              AND so.order_type = 'RETURN'
              AND so.id <> :excludeId
              AND so.overall_status IN (:statuses)
            GROUP BY sol.product_id
            """, nativeQuery = true)
    List<Object[]> sumReturnedByProduct(@Param("originalId") Long originalId,
                                        @Param("excludeId") Long excludeId,
                                        @Param("statuses") List<String> statuses);

}
