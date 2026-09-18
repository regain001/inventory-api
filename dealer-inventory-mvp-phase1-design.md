# Dealer Inventory MVP — Phase 1 Technical Design

Covers: DB schema (Flyway), JPA entities, native-SQL concurrency-safe stock update, Stock In / Stock Out posting services, opening-stock loader, error handling, API surface, and a concurrency test.

**Assumptions** (flag if wrong):
- `dealer` is optional on `stock_out` for MVP (can be null — some sales may not be dealer-attributed yet).
- No DRAFT state — a posted document is final the moment it's saved (matches "ship simple" MVP framing; add DRAFT later if staff need to save half-entered documents).
- Quantities/amounts use `NUMERIC` (via `BigDecimal`), not `int`/`double` — avoids float rounding issues once you add fractional units (kg, litres) later.

---

## 1. Domain model

```
product (1) ───< stock_in_item >─── (1) stock_in
product (1) ───< stock_out_item >─── (1) stock_out ─── (0..1) dealer
product (1) ─── (1) current_stock            [fast-read balance]
product (1) ───< inventory_ledger            [immutable history, source of truth]
app_user (1) ───< stock_in / stock_out / inventory_ledger   [who did it]
```

`current_stock` and `inventory_ledger` are updated **in the same transaction, every time**. `current_stock` is a cache; if it's ever wrong, it can be rebuilt from `SUM(quantity)` in the ledger — that invariant is the whole point of the design.

---

## 2. Database schema — `V1__init_schema.sql`

```sql
CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','STAFF')),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE product (
    id                 BIGSERIAL PRIMARY KEY,
    sku                VARCHAR(50)   NOT NULL UNIQUE,
    product_name       TEXT          NOT NULL,
    unit               VARCHAR(20)   NOT NULL,
    min_stock_level    NUMERIC(18,3) NOT NULL DEFAULT 0,
    active             BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE dealer (
    id           BIGSERIAL PRIMARY KEY,
    dealer_name  VARCHAR(150) NOT NULL,
    phone        VARCHAR(30),
    zone         VARCHAR(50),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Fast-read balance cache. Rebuildable at any time from inventory_ledger.
CREATE TABLE current_stock (
    product_id        BIGINT PRIMARY KEY REFERENCES product(id),
    quantity          NUMERIC(18,3) NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    last_stock_in_at  TIMESTAMPTZ,
    last_stock_out_at TIMESTAMPTZ,
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE SEQUENCE stock_in_doc_seq  START WITH 1;
CREATE SEQUENCE stock_out_doc_seq START WITH 1;

CREATE TABLE stock_in (
    id               BIGSERIAL PRIMARY KEY,
    document_number  VARCHAR(30)   NOT NULL UNIQUE,   -- SI-2026-000001
    do_number        VARCHAR(100),                     -- external company DO ref, preserved as-is
    transaction_date DATE          NOT NULL,
    note             TEXT,
    total_quantity   NUMERIC(18,3) NOT NULL DEFAULT 0,
    total_amount     NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_by       BIGINT        NOT NULL REFERENCES app_user(id),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE stock_in_item (
    id            BIGSERIAL PRIMARY KEY,
    stock_in_id   BIGINT        NOT NULL REFERENCES stock_in(id),
    product_id    BIGINT        NOT NULL REFERENCES product(id),
    quantity      NUMERIC(18,3) NOT NULL CHECK (quantity > 0),
    unit_price    NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_amount  NUMERIC(18,2) NOT NULL DEFAULT 0
);

CREATE TABLE stock_out (
    id               BIGSERIAL PRIMARY KEY,
    document_number  VARCHAR(30)   NOT NULL UNIQUE,   -- SO-2026-000001
    reference        VARCHAR(100),                     -- client's own invoice/challan no, optional
    transaction_date DATE          NOT NULL,
    dealer_id        BIGINT        REFERENCES dealer(id),
    note             TEXT,
    total_quantity   NUMERIC(18,3) NOT NULL DEFAULT 0,
    total_amount     NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_by       BIGINT        NOT NULL REFERENCES app_user(id),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE stock_out_item (
    id             BIGSERIAL PRIMARY KEY,
    stock_out_id   BIGINT        NOT NULL REFERENCES stock_out(id),
    product_id     BIGINT        NOT NULL REFERENCES product(id),
    quantity       NUMERIC(18,3) NOT NULL CHECK (quantity > 0),
    unit_price     NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_amount   NUMERIC(18,2) NOT NULL DEFAULT 0
);

-- Immutable source of truth. Never UPDATE or DELETE rows here.
CREATE TABLE inventory_ledger (
    id                BIGSERIAL PRIMARY KEY,
    product_id        BIGINT        NOT NULL REFERENCES product(id),
    transaction_type  VARCHAR(20)   NOT NULL CHECK (transaction_type IN
                          ('OPENING','STOCK_IN','STOCK_OUT','ADJUSTMENT_IN','ADJUSTMENT_OUT')),
    quantity          NUMERIC(18,3) NOT NULL,           -- signed: + increases stock, - decreases
    reference_type    VARCHAR(20)   NOT NULL,
    reference_id      BIGINT        NOT NULL,
    reference_number  VARCHAR(30)   NOT NULL,
    transaction_date  DATE          NOT NULL,
    balance_after     NUMERIC(18,3) NOT NULL,
    created_by        BIGINT        NOT NULL REFERENCES app_user(id),
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    note              TEXT
);

CREATE INDEX idx_ledger_product_date   ON inventory_ledger (product_id, transaction_date);
CREATE INDEX idx_ledger_reference      ON inventory_ledger (reference_type, reference_id);
CREATE INDEX idx_stock_in_date         ON stock_in (transaction_date);
CREATE INDEX idx_stock_out_date        ON stock_out (transaction_date);
CREATE INDEX idx_stock_out_dealer_date ON stock_out (dealer_id, transaction_date);
```

`ADJUSTMENT_IN`/`ADJUSTMENT_OUT` are in the check constraint now even though the adjustment *workflow* is deferred — costs nothing today, saves a migration later when you add a bare-bones adjustment endpoint.

---

## 3. JPA entities

```java
@Entity
@Table(name = "product")
@Getter @Setter
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String sku;
    @Column(name = "product_name", nullable = false)
    private String productName;
    @Column(nullable = false)
    private String unit;
    @Column(name = "min_stock_level", nullable = false)
    private BigDecimal minStockLevel = BigDecimal.ZERO;
    private boolean active = true;
    @Column(name = "created_at") private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}

@Entity @Table(name = "dealer") @Getter @Setter
public class Dealer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "dealer_name", nullable = false)
    private String dealerName;
    private String phone;
    private String zone;
    private boolean active = true;
}

@Entity @Table(name = "stock_in") @Getter @Setter
public class StockIn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;
    @Column(name = "do_number")
    private String doNumber;
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    private String note;
    @Column(name = "total_quantity") private BigDecimal totalQuantity = BigDecimal.ZERO;
    @Column(name = "total_amount")   private BigDecimal totalAmount = BigDecimal.ZERO;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @Column(name = "created_at") private Instant createdAt;
}

@Entity @Table(name = "stock_in_item") @Getter @Setter
public class StockInItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "stock_in_id", nullable = false) private Long stockInId;
    @Column(name = "product_id", nullable = false)  private Long productId;
    @Column(nullable = false) private BigDecimal quantity;
    @Column(name = "unit_price") private BigDecimal unitPrice = BigDecimal.ZERO;
    @Column(name = "total_amount") private BigDecimal totalAmount = BigDecimal.ZERO;
}

// StockOut / StockOutItem are structurally identical to StockIn / StockInItem,
// plus a nullable dealerId on StockOut and an optional "reference" string.
```

`current_stock` and `inventory_ledger` are deliberately **not** managed as JPA entities for writes — see the repository below. (A read-only `@Entity` / native projection for `inventory_ledger` is fine for the history/report queries; just never `.save()` it outside the ledger insert method.)

---

## 4. Concurrency-safe stock repository (native SQL, Hibernate `Session`)

This is the piece that has to be correct. Two staff creating Stock Out at the same instant must never both succeed if there isn't enough stock for both — a plain `SELECT` then `if (available >= qty) UPDATE` has a race window between the two statements. The fix is a single atomic, conditional `UPDATE`.

```java
@Repository
@RequiredArgsConstructor
public class InventoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private Session session() {
        return entityManager.unwrap(Session.class);
    }

    /** Stock In / returns / positive adjustments. Upserts current_stock, returns new balance. */
    @SuppressWarnings("unchecked")
    public BigDecimal increaseStock(Long productId, BigDecimal qty, Instant now) {
        String sql = """
            INSERT INTO current_stock (product_id, quantity, last_stock_in_at, updated_at)
            VALUES (:productId, :qty, :now, :now)
            ON CONFLICT (product_id)
            DO UPDATE SET quantity = current_stock.quantity + :qty,
                          last_stock_in_at = :now,
                          updated_at = :now
            RETURNING quantity
            """;
        return (BigDecimal) session().createNativeQuery(sql)
                .setParameter("productId", productId)
                .setParameter("qty", qty)
                .setParameter("now", now)
                .getSingleResult();
    }

    /**
     * Stock Out / negative adjustments. Atomically decreases stock ONLY if enough is
     * available — the WHERE clause and the decrement happen as one statement, so two
     * concurrent calls can never both succeed against the same insufficient balance.
     * Returns empty if there wasn't enough stock; caller decides how to report it.
     */
    @SuppressWarnings("unchecked")
    public Optional<BigDecimal> decreaseStockIfAvailable(Long productId, BigDecimal qty, Instant now) {
        String sql = """
            UPDATE current_stock
            SET quantity = quantity - :qty,
                last_stock_out_at = :now,
                updated_at = :now
            WHERE product_id = :productId AND quantity >= :qty
            RETURNING quantity
            """;
        List<BigDecimal> result = session().createNativeQuery(sql)
                .setParameter("productId", productId)
                .setParameter("qty", qty)
                .setParameter("now", now)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public BigDecimal currentQuantity(Long productId) {
        String sql = "SELECT quantity FROM current_stock WHERE product_id = :productId";
        return (BigDecimal) session().createNativeQuery(sql)
                .setParameter("productId", productId)
                .uniqueResultOptional()
                .orElse(BigDecimal.ZERO);
    }

    public void insertLedgerEntry(Long productId, String type, BigDecimal signedQty,
                                   String refType, Long refId, String refNumber,
                                   LocalDate txnDate, BigDecimal balanceAfter,
                                   Long userId, String note) {
        String sql = """
            INSERT INTO inventory_ledger
                (product_id, transaction_type, quantity, reference_type, reference_id,
                 reference_number, transaction_date, balance_after, created_by, created_at, note)
            VALUES
                (:productId, :type, :qty, :refType, :refId, :refNumber, :txnDate, :balance,
                 :userId, now(), :note)
            """;
        session().createNativeQuery(sql)
                .setParameter("productId", productId)
                .setParameter("type", type)
                .setParameter("qty", signedQty)
                .setParameter("refType", refType)
                .setParameter("refId", refId)
                .setParameter("refNumber", refNumber)
                .setParameter("txnDate", txnDate)
                .setParameter("balance", balanceAfter)
                .setParameter("userId", userId)
                .setParameter("note", note)
                .executeUpdate();
    }
}
```

No `@Version`, no pessimistic locks needed — the row-level atomicity of the conditional `UPDATE ... WHERE quantity >= :qty` does the job in one round trip, and Postgres's default read-committed isolation is sufficient here because the write itself is atomic.

---

## 5. Document numbering

```java
@Service
@RequiredArgsConstructor
public class DocumentNumberService {

    @PersistenceContext
    private EntityManager entityManager;

    public String next(String prefix) {
        String seqName = switch (prefix) {
            case "SI" -> "stock_in_doc_seq";
            case "SO" -> "stock_out_doc_seq";
            default -> throw new IllegalArgumentException("Unknown prefix: " + prefix);
        };
        Long nextVal = ((Number) entityManager.unwrap(Session.class)
                .createNativeQuery("SELECT nextval('" + seqName + "')")
                .getSingleResult()).longValue();
        return "%s-%d-%06d".formatted(prefix, Year.now().getValue(), nextVal);
    }
}
```

`seqName` is fed from a fixed internal `switch`, never from request input — safe from injection despite the concatenation.

---

## 6. Stock In posting

```java
@Service
@RequiredArgsConstructor
public class StockInService {

    private final StockInRepository stockInRepo;          // plain Spring Data JPA repo
    private final StockInItemRepository stockInItemRepo;   // plain Spring Data JPA repo
    private final InventoryRepository inventoryRepository;
    private final DocumentNumberService documentNumberService;

    @Transactional
    public StockInResponse postStockIn(StockInRequest request, Long currentUserId) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ValidationException("Stock In must have at least one item.");
        }

        String docNumber = documentNumberService.next("SI");
        Instant now = Instant.now();

        StockIn header = new StockIn();
        header.setDocumentNumber(docNumber);
        header.setDoNumber(request.getDoNumber());
        header.setTransactionDate(request.getTransactionDate());
        header.setNote(request.getNote());
        header.setCreatedBy(currentUserId);
        header.setCreatedAt(now);
        header = stockInRepo.save(header);

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (StockInItemRequest item : request.getItems()) {
            if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
                throw new ValidationException(
                        "Quantity must be greater than zero for product " + item.getProductId());
            }
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal amount = unitPrice.multiply(item.getQuantity());

            StockInItem line = new StockInItem();
            line.setStockInId(header.getId());
            line.setProductId(item.getProductId());
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(unitPrice);
            line.setTotalAmount(amount);
            stockInItemRepo.save(line);

            BigDecimal newBalance = inventoryRepository.increaseStock(
                    item.getProductId(), item.getQuantity(), now);

            inventoryRepository.insertLedgerEntry(
                    item.getProductId(), "STOCK_IN", item.getQuantity(),
                    "STOCK_IN", header.getId(), docNumber,
                    request.getTransactionDate(), newBalance, currentUserId, request.getNote());

            totalQty = totalQty.add(item.getQuantity());
            totalAmount = totalAmount.add(amount);
        }

        header.setTotalQuantity(totalQty);
        header.setTotalAmount(totalAmount);
        stockInRepo.save(header);

        return StockInResponse.from(header);
    }
}
```

---

## 7. Stock Out posting — with the insufficient-stock guard

```java
@Service
@RequiredArgsConstructor
public class StockOutService {

    private final StockOutRepository stockOutRepo;
    private final StockOutItemRepository stockOutItemRepo;
    private final InventoryRepository inventoryRepository;
    private final DocumentNumberService documentNumberService;

    @Transactional
    public StockOutResponse postStockOut(StockOutRequest request, Long currentUserId) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ValidationException("Stock Out must have at least one item.");
        }

        String docNumber = documentNumberService.next("SO");
        Instant now = Instant.now();

        StockOut header = new StockOut();
        header.setDocumentNumber(docNumber);
        header.setReference(request.getReference());
        header.setTransactionDate(request.getTransactionDate());
        header.setDealerId(request.getDealerId());
        header.setNote(request.getNote());
        header.setCreatedBy(currentUserId);
        header.setCreatedAt(now);
        header = stockOutRepo.save(header);

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (StockOutItemRequest item : request.getItems()) {
            if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
                throw new ValidationException(
                        "Quantity must be greater than zero for product " + item.getProductId());
            }

            // Atomic guarded decrement — first failure aborts the whole document (transaction rolls back).
            Optional<BigDecimal> newBalance = inventoryRepository
                    .decreaseStockIfAvailable(item.getProductId(), item.getQuantity(), now);

            if (newBalance.isEmpty()) {
                BigDecimal available = inventoryRepository.currentQuantity(item.getProductId());
                throw new InsufficientStockException(item.getProductId(), available, item.getQuantity());
            }

            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal amount = unitPrice.multiply(item.getQuantity());

            StockOutItem line = new StockOutItem();
            line.setStockOutId(header.getId());
            line.setProductId(item.getProductId());
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(unitPrice);
            line.setTotalAmount(amount);
            stockOutItemRepo.save(line);

            inventoryRepository.insertLedgerEntry(
                    item.getProductId(), "STOCK_OUT", item.getQuantity().negate(),
                    "STOCK_OUT", header.getId(), docNumber,
                    request.getTransactionDate(), newBalance.get(), currentUserId, request.getNote());

            totalQty = totalQty.add(item.getQuantity());
            totalAmount = totalAmount.add(amount);
        }

        header.setTotalQuantity(totalQty);
        header.setTotalAmount(totalAmount);
        stockOutRepo.save(header);

        return StockOutResponse.from(header);
    }
}
```

**Design choice:** if line 3 of a 5-line Stock Out fails on insufficient stock, the whole document rolls back (nothing partially posts) because it's one `@Transactional` method. That matches "auditability" — a Stock Out document is all-or-nothing, never half-posted.

### Exception + error shape

```java
public class InsufficientStockException extends RuntimeException {
    @Getter private final Long productId;
    @Getter private final BigDecimal available;
    @Getter private final BigDecimal requested;

    public InsufficientStockException(Long productId, BigDecimal available, BigDecimal requested) {
        super("Insufficient stock. Available: %s, requested: %s.".formatted(available, requested));
        this.productId = productId;
        this.available = available;
        this.requested = requested;
    }
}

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.unprocessableEntity().body(Map.of(
                "code", "INSUFFICIENT_STOCK",
                "message", ex.getMessage(),
                "details", Map.of("productId", ex.getProductId(),
                                   "available", ex.getAvailable(),
                                   "requested", ex.getRequested())
        ));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", "VALIDATION_ERROR", "message", ex.getMessage()));
    }
}
```

---

## 8. Opening stock loader (needed for go-live, not the full import wizard)

The client has existing stock the day this system launches — that has to load in as `OPENING` ledger entries, not silently seeded into `current_stock`.

```java
@Service
@RequiredArgsConstructor
public class OpeningStockService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void loadOpeningStock(Long productId, BigDecimal qty, LocalDate asOfDate, Long userId) {
        if (qty.signum() < 0) throw new ValidationException("Opening quantity cannot be negative.");
        Instant now = Instant.now();
        BigDecimal newBalance = inventoryRepository.increaseStock(productId, qty, now);
        inventoryRepository.insertLedgerEntry(
                productId, "OPENING", qty, "OPENING", productId, "OPENING",
                asOfDate, newBalance, userId, "Opening stock load");
    }
}
```

For MVP, drive this from a plain CSV (`productCode,quantity`) parsed row-by-row through this same method — no preview/validate UI yet. That's the whole "import" feature until v2.

---

## 9. Phase 1 API surface

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/login` | Authenticate, return JWT/session |
| GET/POST/PUT | `/api/products` , `/api/products/{id}` | Product CRUD |
| GET/POST | `/api/dealers` | Dealer CRUD (minimal) |
| POST | `/api/stock-in` | Post a Stock In document |
| GET | `/api/stock-in`, `/api/stock-in/{id}` | List / view |
| POST | `/api/stock-out` | Post a Stock Out document |
| GET | `/api/stock-out`, `/api/stock-out/{id}` | List / view |
| POST | `/api/inventory/opening-stock` | Load opening balance for a product |
| GET | `/api/inventory/current-stock` | Paginated current stock, search + status filter |
| GET | `/api/inventory/{productId}/ledger` | Full transaction history for a product |
| GET | `/api/reports/stock-summary` | Opening/In/Out/Closing by date range |
| GET | `/api/dashboard/summary` | Totals, today's in/out, low-stock count |

---

## 10. Concurrency test (proves the guard actually works)

```java
@SpringBootTest
class StockOutConcurrencyTest {

    @Autowired InventoryRepository inventoryRepository;
    @Autowired StockOutService stockOutService;

    @Test
    void onlyOneOfTwoConcurrentOversellsShouldSucceed() throws Exception {
        Long productId = seedProductWithStock(BigDecimal.TEN); // current_stock = 10

        ExecutorService pool = Executors.newFixedThreadPool(2);
        Callable<Boolean> requestSeven = () -> tryPost(productId, new BigDecimal("7"));
        Callable<Boolean> requestSix   = () -> tryPost(productId, new BigDecimal("6"));

        List<Future<Boolean>> results = pool.invokeAll(List.of(requestSeven, requestSix));
        long successCount = results.stream().filter(f -> get(f)).count();

        assertThat(successCount).isEqualTo(1); // exactly one must succeed
        assertThat(inventoryRepository.currentQuantity(productId))
                .isIn(new BigDecimal("3"), new BigDecimal("4")); // 10-7 or 10-6
    }

    private boolean tryPost(Long productId, BigDecimal qty) {
        try {
            stockOutService.postStockOut(buildRequest(productId, qty), 1L);
            return true;
        } catch (InsufficientStockException e) {
            return false;
        }
    }
}
```

Run this against the real Postgres (Testcontainers), not H2 — the guarantee comes from Postgres's row-level atomicity on the conditional `UPDATE`, which H2 won't reliably exercise the same way.

---

## What's next

This covers phase 1 core (schema + posting). Still open from the earlier scope: Current Stock page query (with status derivation), Stock Summary report query, dashboard aggregates, and auth/security config. Say which one and I'll detail it the same way.
