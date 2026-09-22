package com.ecom.inventory.service;

// package + imports for your own project classes omitted
// (UserInputValidationException, PaginationDto, NestedPojoTransformer, Product, ProductRepository,
//  CustomerRepository, InventoryRepository, DTOs, entities, repositories, CurrentUser)
import com.ecom.inventory.component.CurrentUser;
import com.ecom.inventory.dto.common.PaginationDto;
import com.ecom.inventory.dto.sales_order.*;
import com.ecom.inventory.entity.Product;
import com.ecom.inventory.entity.SalesOrder;
import com.ecom.inventory.entity.SalesOrderPerline;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.repository.*;
import com.ecom.inventory.util.db.NestedPojoTransformer;
import com.ecom.inventory.util.db.PojoTransformerException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_COMPLETED = "Completed";
    private static final String TYPE_SALE = "SALE";
    private static final String TYPE_RETURN = "RETURN";
    private static final String DEFAULT_CUSTOMER_TYPE = "PRAN RFL";
    private static final Set<String> CUSTOMER_TYPES = Set.of("PRAN RFL", "MAHTAB MACHINERIES");
    private static final Set<String> STATUSES = Set.of(STATUS_PENDING, STATUS_COMPLETED);
    private static final Set<String> ORDER_TYPES = Set.of(TYPE_SALE, TYPE_RETURN, "EXCHANGE");
    private static final int MAX_REFERENCE_LENGTH = 100;
    private static final int MAX_MOBILE_LENGTH = 20;
    private static final int MAX_PAGE_SIZE = 200;

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderPerlineRepository salesOrderPerlineRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InventoryRepository inventoryRepository;
    private final CurrentUser currentUser;
    private final EntityManager em;

    // Column order here MUST match DETAIL_SELECT.
    private static final String[] DETAIL_FIELDS = {
            "salesOrderId", "documentNumber", "reference", "orderDate", "customerId", "customerType",
            "orderType", "originalSalesOrderId", "totalQuantity", "discountAmount", "discountPercentage", "totalAmount",
            "overallStatus", "note", "deliveryAddress", "contactPersonMobileNumber", "createdBy", "createdAt",

            "perlines.id", "perlines.productId", "perlines.productCode", "perlines.productName",
            "perlines.quantity", "perlines.unitOfMeasure", "perlines.basePrice", "perlines.discountAmount",
            "perlines.discountPercentage", "perlines.unitPrice", "perlines.extendedPrice", "perlines.netAmount"
    };

    private static final String DETAIL_SELECT = """
            SELECT
                so.id                           AS salesOrderId,
                so.document_number              AS documentNumber,
                so.reference                    AS reference,
                so.order_date                   AS orderDate,
                so.customer_id                  AS customerId,
                so.customer_type                AS customerType,
                so.order_type                   AS orderType,
                so.original_sales_order_id      AS originalSalesOrderId,
                so.total_quantity               AS totalQuantity,
                so.discount_amount              AS discountAmount,
                so.discount_percentage          AS orderDiscountPercentage,
                so.total_amount                 AS totalAmount,
                so.overall_status               AS overallStatus,
                so.note                         AS note,
                so.delivery_address             AS deliveryAddress,
                so.contact_person_mobile_number AS contactPersonMobileNumber,
                so.created_by                   AS createdBy,
                so.created_at                   AS createdAt,

                sol.id                          AS id,
                sol.product_id                  AS productId,
                p.sku                           AS productCode,
                p.product_name                  AS productName,
                sol.quantity                    AS quantity,
                sol.unit_of_measure             AS unitOfMeasure,
                sol.base_price                  AS basePrice,
                sol.discount_amount             AS lineDiscountAmount,
                sol.discount_percentage         AS discountPercentage,
                sol.unit_price                  AS unitPrice,
                sol.extended_price              AS extendedPrice,
                sol.net_amount                  AS netAmount
            FROM sales_order so
            LEFT JOIN sales_order_perline sol ON sol.sales_order_id = so.id
            LEFT JOIN product p ON p.id = sol.product_id
            """;

    // =====================================================================
    // Read
    // =====================================================================

    @Transactional(readOnly = true)
    public PaginationDto getSalesOrderList(SalesOrderQ params) throws UserInputValidationException, PojoTransformerException {

        validateList(params);
        Filter filter = buildFilter(params);

        String sql = DETAIL_SELECT
                + "WHERE so.id IN (\n"
                + "    SELECT f.id FROM sales_order f\n"
                + filter.where()
                + "    ORDER BY f.order_date DESC, f.id DESC\n"
                + "    LIMIT :limit OFFSET :offset\n"
                + ")\n"
                + "ORDER BY so.order_date DESC, so.id DESC, sol.id ASC";

        Map<String, Object> bind = new HashMap<>(filter.params());
        bind.put("limit", params.getLimit());
        bind.put("offset", params.getStart());

        List<SalesOrderDto> records = runDetailQuery(sql, bind);

        PaginationDto ret = new PaginationDto();
        ret.setTotalRecords(countSalesOrders(filter));
        ret.setFetchedRecords(records.size());
        ret.setStart(params.getStart());
        ret.setLimit(params.getLimit());
        ret.setRecords(records);
        return ret;
    }

    @Transactional(readOnly = true)
    public SalesOrderDto getSalesOrderById(Long id) throws UserInputValidationException, PojoTransformerException {

        String sql = DETAIL_SELECT + "WHERE so.id = :id\nORDER BY sol.id ASC";
        List<SalesOrderDto> list = runDetailQuery(sql, Map.of("id", id));

        if (list.isEmpty()) {
            throw new UserInputValidationException("Sales order not found with id: " + id);
        }
        return list.get(0);
    }

    // =====================================================================
    // Create / update (always leaves the order Pending)
    // =====================================================================

    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdateSalesOrder(SalesOrderSaveDto dto) throws UserInputValidationException {

        // TODO: role check (ADMIN only)

        // 1. Everything that can fail is checked BEFORE the first write.
        validateSave(dto);
        String orderType = orderTypeOf(dto);
        String customerType = StringUtils.hasText(dto.getCustomerType())
                ? dto.getCustomerType().trim().toUpperCase() : DEFAULT_CUSTOMER_TYPE;

        if (!customerRepository.existsById(dto.getCustomerId())) {
            throw new UserInputValidationException("Customer not found with id: " + dto.getCustomerId());
        }

        Map<Long, Product> products = loadProducts(dto.getLines());

        List<SalesOrderCalculator.LineInput> inputs = new ArrayList<>();
        for (SalesOrderLineSaveDto l : dto.getLines()) {
            Product p = products.get(l.getProductId());
            inputs.add(new SalesOrderCalculator.LineInput(
                    p.getId(), l.getQuantity(), basePriceOf(p), l.getDiscountAmount(), l.getDiscountPercentage(), uomOf(p)));
        }
        SalesOrderCalculator.OrderResult calc = SalesOrderCalculator.calculate(
                inputs, dto.getDiscountAmount(), dto.getDiscountPercentage());

        boolean isUpdate = dto.getSalesOrderId() != null && dto.getSalesOrderId() > 0;
        SalesOrder order;

        if (isUpdate) {
            order = salesOrderRepository.findByIdForUpdate(dto.getSalesOrderId())
                    .orElseThrow(() -> new UserInputValidationException(
                            "Sales order not found with id: " + dto.getSalesOrderId()));
            if (!STATUS_PENDING.equals(order.getOverallStatus())) {
                throw new UserInputValidationException("Only pending sales orders can be modified");
            }
        } else {
            order = new SalesOrder();
        }

        validateReturn(orderType, dto, isUpdate ? order.getId() : 0L);

        // 2. Header
        if (!isUpdate) {
            order.setDocumentNumber(generateDocumentNumber());
            order.setOverallStatus(STATUS_PENDING);
            order.setCreatedAt(LocalDateTime.now());
            order.setCreatedBy(currentUser.id());
        }
        order.setReference(trimToNull(dto.getReference()));
        order.setOrderDate(dto.getOrderDate());
        order.setCustomerId(dto.getCustomerId());
        order.setCustomerType(customerType);
        order.setOrderType(orderType);
        order.setOriginalSalesOrderId(TYPE_RETURN.equals(orderType) ? dto.getOriginalSalesOrderId() : null);
        order.setNote(dto.getNote());
        order.setDeliveryAddress(dto.getDeliveryAddress());
        order.setContactPersonMobileNumber(trimToNull(dto.getContactPersonMobileNumber()));
        order.setTotalQuantity(calc.totalQuantity());
        order.setDiscountApplied(calc.discountApplied());
        order.setDiscountAmount(calc.discountAmount());
        order.setDiscountPercentage(calc.discountPercentage());
        order.setTotalAmount(calc.totalAmount());

        SalesOrder saved = salesOrderRepository.save(order);

        // 3. Lines (replace-all; the perline delete flushes + clears the persistence context)
        if (isUpdate) {
            salesOrderPerlineRepository.deleteBySalesOrderId(saved.getId());
        }

        List<SalesOrderPerline> lines = new ArrayList<>();
        for (SalesOrderCalculator.LineResult r : calc.lines()) {
            SalesOrderPerline line = new SalesOrderPerline();
            line.setSalesOrderId(saved.getId());
            line.setProductId(r.productId());
            line.setQuantity(r.quantity());
            line.setBasePrice(r.basePrice());
            line.setDiscountApplied(r.discountApplied());
            line.setDiscountAmount(r.discountAmount());
            line.setDiscountPercentage(r.discountPercentage());
            line.setUnitPrice(r.unitPrice());
            line.setExtendedPrice(r.extendedPrice());
            line.setNetAmount(r.netAmount());
            line.setUnitOfMeasure(r.unitOfMeasure());
            lines.add(line);
        }
        salesOrderPerlineRepository.saveAll(lines);

        return saved.getId();
    }

    // =====================================================================
    // Complete: the only place inventory is touched. Idempotent.
    // =====================================================================

    @Transactional(rollbackFor = Exception.class)
    public String completeSalesOrder(Long id) throws UserInputValidationException {

        // TODO: role check

        SalesOrder order = salesOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new UserInputValidationException("Sales order not found with id: " + id));

        if (STATUS_COMPLETED.equals(order.getOverallStatus())) {
            return "Sales order is already completed";
        }

        List<SalesOrderPerline> lines = new ArrayList<>(salesOrderPerlineRepository.findBySalesOrderIdOrderById(id));
        if (lines.isEmpty()) {
            throw new UserInputValidationException("Sales order must contain at least one product");
        }
        // Consistent lock order across concurrent orders avoids inventory-row deadlocks.
        lines.sort(Comparator.comparing(SalesOrderPerline::getProductId));

        // Referential checks (no FKs in the DB): customer and products must still exist.
        if (!customerRepository.existsById(order.getCustomerId())) {
            throw new UserInputValidationException("Customer not found with id: " + order.getCustomerId());
        }
        Set<Long> productIds = lines.stream().map(SalesOrderPerline::getProductId).collect(Collectors.toSet());
        if (productRepository.findAllById(productIds).size() != productIds.size()) {
            throw new UserInputValidationException("One or more products on this sales order no longer exist");
        }

        boolean isReturn = TYPE_RETURN.equals(order.getOrderType());

        if (isReturn) {
            // Serialise all returns against the same original sale, then re-check with COMPLETED returns only.
            salesOrderRepository.findByIdForUpdate(order.getOriginalSalesOrderId())
                    .orElseThrow(() -> new UserInputValidationException("Original sales order not found"));
            Map<Long, Integer> requested = lines.stream().collect(
                    Collectors.toMap(SalesOrderPerline::getProductId, SalesOrderPerline::getQuantity));
            assertWithinSold(order.getOriginalSalesOrderId(), requested, order.getId(), List.of(STATUS_COMPLETED));
        }

        LocalDateTime now = LocalDateTime.now();
        Long userId = currentUser.id();

        for (SalesOrderPerline line : lines) {
            Integer balance;
            String movement;
            String refType;

            if (isReturn) {
                balance = inventoryRepository.increaseStock(line.getProductId(), line.getQuantity(), now);
                movement = "STOCK_IN";
                refType = "SALES_RETURN";
            } else {
                // Empty = no stock row or not enough stock. The exception rolls back every
                // decrement made so far in this transaction.
                balance = inventoryRepository
                        .decreaseStockIfAvailable(line.getProductId(), line.getQuantity(), now)
                        .orElseThrow(() -> new UserInputValidationException(
                                "Insufficient stock for product id: " + line.getProductId()));
                movement = "STOCK_OUT";
                refType = "SALES_ORDER";
            }

            inventoryRepository.insertLedgerEntry(
                    line.getProductId(), movement, line.getQuantity(),
                    refType, order.getId(), order.getDocumentNumber(),
                    order.getOrderDate(), balance, userId, "");
        }

        order.setOverallStatus(STATUS_COMPLETED);
        salesOrderRepository.save(order);

        return "Sales order completed successfully";
    }

    // =====================================================================
    // Delete (Pending only)
    // =====================================================================

    @Transactional(rollbackFor = Exception.class)
    public void deleteSalesOrder(Long id) throws UserInputValidationException {

        // TODO: role check (ADMIN only)

        SalesOrder order = salesOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new UserInputValidationException("Sales order not found with id: " + id));

        if (!STATUS_PENDING.equals(order.getOverallStatus())) {
            throw new UserInputValidationException("Only pending sales orders can be deleted");
        }
        if (salesOrderRepository.existsByOriginalSalesOrderId(id)) {
            throw new UserInputValidationException("Sales order is referenced by a return and cannot be deleted");
        }

        salesOrderPerlineRepository.deleteBySalesOrderId(id);
        salesOrderRepository.deleteById(id);
    }

    // =====================================================================
    // Validation
    // =====================================================================

    private void validateList(SalesOrderQ q) throws UserInputValidationException {
        UserInputValidationException e = new UserInputValidationException();

        if (q.getStart() == null || q.getStart() < 0) {
            e.addErrorMessage("Sales Order", "Start must be 0 or greater");
        }
        if (q.getLimit() == null || q.getLimit() <= 0 || q.getLimit() > MAX_PAGE_SIZE) {
            e.addErrorMessage("Sales Order", "Limit must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (StringUtils.hasText(q.getOverallStatus()) && !STATUSES.contains(q.getOverallStatus().trim())) {
            e.addErrorMessage("Sales Order", "Status must be one of " + STATUSES);
        }
        if (StringUtils.hasText(q.getOrderType()) && !ORDER_TYPES.contains(q.getOrderType().trim().toUpperCase())) {
            e.addErrorMessage("Sales Order", "Order type must be one of " + ORDER_TYPES);
        }
        if (q.getFromDate() != null && q.getToDate() != null && q.getFromDate().isAfter(q.getToDate())) {
            e.addErrorMessage("Sales Order", "From date cannot be after to date");
        }
        if (e.isValidationErrorOccured()) {
            throw e;
        }
    }

    private void validateSave(SalesOrderSaveDto dto) throws UserInputValidationException {
        UserInputValidationException e = new UserInputValidationException();

        if (dto.getOrderDate() == null) {
            e.addErrorMessage("Sales Order", "Order date can't be empty");
        }
        if (dto.getCustomerId() == null) {
            e.addErrorMessage("Sales Order", "Customer can't be empty");
        }
        if (StringUtils.hasText(dto.getCustomerType())
                && !CUSTOMER_TYPES.contains(dto.getCustomerType().trim().toUpperCase())) {
            e.addErrorMessage("Sales Order", "Customer type must be one of " + CUSTOMER_TYPES);
        }
        if (dto.getSalesOrderId() != null && dto.getSalesOrderId() < 0) {
            e.addErrorMessage("Sales Order", "Sales order id is invalid");
        }
        if (dto.getReference() != null && dto.getReference().trim().length() > MAX_REFERENCE_LENGTH) {
            e.addErrorMessage("Sales Order", "Reference cannot exceed " + MAX_REFERENCE_LENGTH + " characters");
        }
        if (dto.getContactPersonMobileNumber() != null
                && dto.getContactPersonMobileNumber().trim().length() > MAX_MOBILE_LENGTH) {
            e.addErrorMessage("Sales Order", "Contact mobile number cannot exceed " + MAX_MOBILE_LENGTH + " characters");
        }
        if (dto.getOriginalSalesOrderId() != null && dto.getOriginalSalesOrderId() <= 0) {
            e.addErrorMessage("Sales Order", "Original sales order id is invalid");
        }
        validateDiscount(e, "Order", dto.getDiscountAmount(), dto.getDiscountPercentage());

        String orderType = orderTypeOf(dto);
        if (!TYPE_SALE.equals(orderType) && !TYPE_RETURN.equals(orderType)) {
            e.addErrorMessage("Sales Order", "Order type '" + orderType + "' is not supported (SALE or RETURN)");
        }

        if (dto.getLines() == null || dto.getLines().isEmpty()) {
            e.addErrorMessage("Sales Order", "Sales order must contain at least one product");
        } else {
            Set<Long> seen = new HashSet<>();
            for (SalesOrderLineSaveDto line : dto.getLines()) {
                if (line.getProductId() == null) {
                    e.addErrorMessage("Sales Order", "Product is required");
                } else if (!seen.add(line.getProductId())) {
                    e.addErrorMessage("Sales Order", "Product " + line.getProductId() + " appears more than once");
                }
                if (line.getQuantity() == null || line.getQuantity() <= 0) {
                    e.addErrorMessage("Sales Order", "Quantity must be greater than zero");
                }
                validateDiscount(e, "Line", line.getDiscountAmount(), line.getDiscountPercentage());
            }
        }

        if (e.isValidationErrorOccured()) {
            throw e;
        }
    }

    /** Shape checks shared by order- and line-level discounts (cross-field limits live in the calculator). */
    private static void validateDiscount(UserInputValidationException e, String scope, Double amount, Double pct) {
        if (amount != null && pct != null) {
            e.addErrorMessage("Sales Order", scope + " discount: send either an amount or a percentage, not both");
        }
        if (amount != null && amount < 0) {
            e.addErrorMessage("Sales Order", scope + " discount amount cannot be negative");
        }
        if (pct != null && (pct < 0 || pct > 100)) {
            e.addErrorMessage("Sales Order", scope + " discount percentage must be between 0 and 100");
        }
    }

    /** RETURN must reference a completed SALE of the same customer, and stay within sold quantities. */
    private void validateReturn(String orderType, SalesOrderSaveDto dto, Long excludeOrderId)
            throws UserInputValidationException {

        if (!TYPE_RETURN.equals(orderType)) {
            if (dto.getOriginalSalesOrderId() != null) {
                throw new UserInputValidationException("Original sales order is only allowed for returns");
            }
            return;
        }

        if (dto.getOriginalSalesOrderId() == null) {
            throw new UserInputValidationException("Original sales order is required for a return");
        }

        SalesOrder original = salesOrderRepository.findById(dto.getOriginalSalesOrderId())
                .orElseThrow(() -> new UserInputValidationException(
                        "Original sales order not found with id: " + dto.getOriginalSalesOrderId()));

        if (!TYPE_SALE.equals(original.getOrderType()) || !STATUS_COMPLETED.equals(original.getOverallStatus())) {
            throw new UserInputValidationException("A return must reference a completed sale");
        }
        if (!original.getCustomerId().equals(dto.getCustomerId())) {
            throw new UserInputValidationException("Customer does not match the original sales order");
        }

        Map<Long, Integer> requested = dto.getLines().stream().collect(
                Collectors.toMap(SalesOrderLineSaveDto::getProductId, SalesOrderLineSaveDto::getQuantity));

        // At save time count Pending + Completed returns (conservative).
        assertWithinSold(original.getId(), requested, excludeOrderId, List.of(STATUS_PENDING, STATUS_COMPLETED));
    }

    private void assertWithinSold(Long originalId, Map<Long, Integer> requested,
                                  Long excludeOrderId, List<String> countedStatuses)
            throws UserInputValidationException {

        Map<Long, Long> sold = salesOrderPerlineRepository.findBySalesOrderIdOrderById(originalId).stream()
                .collect(Collectors.toMap(SalesOrderPerline::getProductId,
                        l -> l.getQuantity().longValue(), Long::sum));

        Map<Long, Long> returned = new HashMap<>();
        for (Object[] row : salesOrderPerlineRepository.sumReturnedByProduct(originalId, excludeOrderId, countedStatuses)) {
            returned.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        for (Map.Entry<Long, Integer> entry : requested.entrySet()) {
            long soldQty = sold.getOrDefault(entry.getKey(), 0L);
            if (soldQty == 0) {
                throw new UserInputValidationException(
                        "Product id " + entry.getKey() + " was not part of the original sale");
            }
            long remaining = soldQty - returned.getOrDefault(entry.getKey(), 0L);
            if (entry.getValue() > remaining) {
                throw new UserInputValidationException("Return quantity for product id " + entry.getKey()
                        + " exceeds the remaining returnable quantity (" + remaining + ")");
            }
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private record Filter(String where, Map<String, Object> params) {}

    /** One filter definition shared by the list query and the count query. Alias is always "f". */
    private Filter buildFilter(SalesOrderQ q) {
        StringBuilder w = new StringBuilder("    WHERE 1=1\n");
        Map<String, Object> p = new HashMap<>();

        if (StringUtils.hasText(q.getKeyword())) {
            w.append("    AND (f.document_number ILIKE :kw OR f.reference ILIKE :kw)\n");
            p.put("kw", "%" + escapeLike(q.getKeyword().trim()) + "%");
        }
        if (StringUtils.hasText(q.getOverallStatus())) {
            w.append("    AND f.overall_status = :status\n");
            p.put("status", q.getOverallStatus().trim());
        }
        if (StringUtils.hasText(q.getOrderType())) {
            w.append("    AND f.order_type = :orderType\n");
            p.put("orderType", q.getOrderType().trim().toUpperCase());
        }
        if (q.getCustomerId() != null) {
            w.append("    AND f.customer_id = :customerId\n");
            p.put("customerId", q.getCustomerId());
        }
        if (q.getFromDate() != null) {
            w.append("    AND f.order_date >= :fromDate\n");
            p.put("fromDate", q.getFromDate());
        }
        if (q.getToDate() != null) {
            w.append("    AND f.order_date <= :toDate\n");      // order_date is a DATE: inclusive is correct
            p.put("toDate", q.getToDate());
        }
        return new Filter(w.toString(), p);
    }

    private int countSalesOrders(Filter filter) {
        Query q = em.createNativeQuery("SELECT COUNT(*) FROM sales_order f\n" + filter.where());
        filter.params().forEach(q::setParameter);
        return ((Number) q.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    private List<SalesOrderDto> runDetailQuery(String sql, Map<String, Object> bind) throws PojoTransformerException {
        Query q = em.createNativeQuery(sql);
        bind.forEach(q::setParameter);
        List<Object[]> rows = q.getResultList();

        return NestedPojoTransformer.transformList(
                SalesOrderDto.class, SalesOrderPerlineDto.class, rows, DETAIL_FIELDS, "salesOrderId");
    }

    private Map<Long, Product> loadProducts(List<SalesOrderLineSaveDto> lines) throws UserInputValidationException {
        Set<Long> ids = lines.stream().map(SalesOrderLineSaveDto::getProductId).collect(Collectors.toSet());
        Map<Long, Product> found = productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Long> missing = ids.stream().filter(i -> !found.containsKey(i)).sorted().toList();
        if (!missing.isEmpty()) {
            throw new UserInputValidationException("Product not found with id: " + missing);
        }
        return found;
    }

    private String generateDocumentNumber() {
        LocalDate today = LocalDate.now();
        String prefix = "SO"
                + String.format("%02d", today.getMonthValue())
                + String.format("%02d", today.getYear() % 100);      // SO0926
        Long seq = salesOrderRepository.nextDocumentSequence(prefix);
        return prefix + String.format("%05d", seq);                  // SO092600001
    }

    private static String orderTypeOf(SalesOrderSaveDto dto) {
        return StringUtils.hasText(dto.getOrderType()) ? dto.getOrderType().trim().toUpperCase() : TYPE_SALE;
    }

    private static String trimToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    // ADAPT these two to your Product entity (selling price + unit of measure getters).
    private static double basePriceOf(Product p) {
        return p.getPrice();
    }

    private static String uomOf(Product p) {
        return p.getPackUnit();
    }
}