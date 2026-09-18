package com.ecom.inventory.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private Session session() { return entityManager.unwrap(Session.class); }

    /** Stock In / returns / positive adjustments. Upserts current_stock, returns new balance. */
    public Integer increaseStock(Long productId, Integer qty, LocalDateTime now) {
        String sql = """
            INSERT INTO current_stock (product_id, quantity, last_stock_in_at, updated_at)
            VALUES (:productId, :qty, :now, :now)
            ON CONFLICT (product_id)
            DO UPDATE SET quantity = current_stock.quantity + :qty,
                          last_stock_in_at = :now, updated_at = :now
            RETURNING quantity
            """;
        return (Integer) session().createNativeQuery(sql)
                .setParameter("productId", productId).setParameter("qty", qty)
                .setParameter("now", now).getSingleResult();
    }

    /** Stock Out / negative adjustments. Atomic guarded decrement — never oversells under concurrency. */
    public Optional<Integer> decreaseStockIfAvailable(Long productId, Integer qty, LocalDateTime now) {
        String sql = """
            UPDATE current_stock
            SET quantity = quantity - :qty, last_stock_out_at = :now, updated_at = :now
            WHERE product_id = :productId AND quantity >= :qty
            RETURNING quantity
            """;
        List<Integer> result = session().createNativeQuery(sql)
                .setParameter("productId", productId).setParameter("qty", qty)
                .setParameter("now", now).getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public Integer currentQuantity(Long productId) {
        String sql = "SELECT quantity FROM current_stock WHERE product_id = :productId";
        return (Integer) session().createNativeQuery(sql)
                .setParameter("productId", productId).uniqueResultOptional().orElse(0);
    }

    public void insertLedgerEntry(Long productId, String type, Integer signedQty,
                                  String refType, Long refId, String refNumber, LocalDate txnDate,
                                  Integer balanceAfter, Long userId, String note) {
        String sql = """
            INSERT INTO inventory_ledger
                (product_id, transaction_type, quantity, reference_type, reference_id,
                 reference_number, transaction_date, balance_after, created_by, created_at, note)
            VALUES (:productId, :type, :qty, :refType, :refId, :refNumber, :txnDate, :balance,
                    :userId, now(), :note)
            """;
        session().createNativeQuery(sql)
                .setParameter("productId", productId).setParameter("type", type)
                .setParameter("qty", signedQty).setParameter("refType", refType)
                .setParameter("refId", refId).setParameter("refNumber", refNumber)
                .setParameter("txnDate", txnDate).setParameter("balance", balanceAfter)
                .setParameter("userId", userId).setParameter("note", note)
                .executeUpdate();
    }
}
