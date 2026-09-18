package com.ecom.inventory.service;

import com.ecom.inventory.dto.common.PaginationDto;
import com.ecom.inventory.dto.purchase_order.*;
import com.ecom.inventory.entity.InventoryLedger;
import com.ecom.inventory.entity.Product;
import com.ecom.inventory.entity.PurchaseOrder;
import com.ecom.inventory.entity.PurchaseOrderPerline;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.repository.InventoryRepository;
import com.ecom.inventory.repository.ProductRepository;
import com.ecom.inventory.repository.PurchaseOrderPerLineRepository;
import com.ecom.inventory.repository.PurchaseOrderRepository;
import com.ecom.inventory.util.db.DtoResultTransformer;
import com.ecom.inventory.util.db.NestedPojoTransformer;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderPerLineRepository purchaseOrderPerLineRepository;
    private final ProductRepository productRepository;
    private final EntityManager em;
    private final InventoryRepository inventoryRepository;

    public PaginationDto getPurchaseOrderList(PurchaseOrderQ params) throws UserInputValidationException, Exception {

        Session session = em.unwrap(Session.class);
        PaginationDto ret = new PaginationDto();
        validateGetPurchaseOrderList(params);

        try {

            String queryStr =
                    "SELECT \n"
                            + "po.id                         AS purchaseOrderId,\n"
                            + "po.document_number            AS documentNumber,\n"
                            + "po.po_number                  AS poNumber,\n"
                            + "po.transaction_date::DATE     AS transactionDate,\n"
                            + "po.note                       AS note,\n"
                            + "po.total_quantity             AS totalQuantity,\n"
                            + "po.total_amount               AS totalAmount,\n"
                            + "po.overall_status             AS overallStatus,\n"
                            + "po.created_by                 AS createdBy,\n"
                            + "po.created_at                 AS createdAt,\n"

                            + "pol.id                        AS id,\n"
                            + "pol.product_id                AS productId,\n"
                            + "p.sku                         AS productCode,\n"
                            + "p.product_name                AS productName,\n"
                            + "pol.quantity                  AS quantity,\n"
                            + "pol.unit_price                AS unitPrice,\n"
                            + "pol.total_amount              AS perlineTotalAmount\n"

                            + "FROM purchase_order po\n"
                            + "LEFT JOIN purchase_order_perline pol ON pol.purchase_order_id = po.id\n"
                            + "LEFT JOIN product p ON p.id = pol.product_id\n"
                            + "WHERE po.id IN (\n"
                            + "    SELECT po2.id\n"
                            + "    FROM purchase_order po2\n"
                            + "    WHERE 1=1\n";

            if (params.getKeyword() != null && !params.getKeyword().trim().isEmpty()) {
                String keyword = params.getKeyword().trim();
                queryStr += "AND (po2.document_number ILIKE '%" + keyword + "%' " + "OR po2.po_number ILIKE '%" + keyword + "%')\n";
            }

            if (params.getOverallStatus() != null && !params.getOverallStatus().trim().isEmpty()) {

                queryStr += "AND po2.overall_status = '" + params.getOverallStatus().trim() + "'\n";
            }

            if (params.getFromDate() != null) {
                queryStr += "AND po2.transaction_date >= '" + params.getFromDate() + "'\n";
            }

            if (params.getToDate() != null) {
                queryStr += "AND po2.transaction_date < '" + params.getToDate().plusDays(1) + "'\n";
            }

            queryStr += "ORDER BY po2.transaction_date DESC, po2.id DESC\n"
                            + "LIMIT " + params.getLimit() + "\n"
                            + "OFFSET " + params.getStart() + "\n"
                            + ")\n"

                            + "ORDER BY po.transaction_date DESC, "
                            + "po.id DESC, pol.id ASC";

            Integer totalRecords = getPurchaseOrderListCount(params);

            List<Object[]> result = session.createNativeQuery(queryStr).list();

            List<PurchaseOrderDto> list =
                    NestedPojoTransformer.transformList(
                            PurchaseOrderDto.class,
                            PurchaseOrderPerlineDto.class,
                            result,
                            new String[]{
                                    "purchaseOrderId",
                                    "documentNumber",
                                    "poNumber",
                                    "transactionDate",
                                    "note",
                                    "totalQuantity",
                                    "totalAmount",
                                    "overallStatus",
                                    "createdBy",
                                    "createdAt",

                                    "perlines.id",
                                    "perlines.productId",
                                    "perlines.productCode",
                                    "perlines.productName",
                                    "perlines.quantity",
                                    "perlines.unitPrice",
                                    "perlines.perlineTotalAmount"
                            },
                            "purchaseOrderId"
                    );

            ret.setTotalRecords(totalRecords);
            ret.setFetchedRecords(list.size());
            ret.setStart(params.getStart());
            ret.setLimit(params.getLimit());
            ret.setRecords(list);

            return ret;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void validateGetPurchaseOrderList(PurchaseOrderQ params) throws UserInputValidationException {

        UserInputValidationException e = new UserInputValidationException();

        if (params.getStart() == null) {
            e.addErrorMessage("Purchase Order", "Start can't be empty");
        }

        if (params.getLimit() == null) {
            e.addErrorMessage("Purchase Order", "Limit can't be empty");
        }

        if (e.isValidationErrorOccured()) {
            throw e;
        }
    }

    private String getPurchaseOrderListQueryBody(PurchaseOrderQ params) {

        String queryStr = "FROM purchase_order po \n" +
                "WHERE 1=1 \n";

        if (params.getKeyword() != null && !params.getKeyword().trim().isEmpty()) {
            String kw = params.getKeyword().trim();
            queryStr += "AND (po.document_number ILIKE '%" + kw + "%' " + "OR po.po_number ILIKE '%" + kw + "%') \n";
        }

        if (params.getOverallStatus() != null && !params.getOverallStatus().trim().isEmpty()) {
            queryStr += "AND po.overall_status = '" + params.getOverallStatus().trim() + "' \n";
        }

        if (params.getFromDate() != null) {
            queryStr += "AND po.transaction_date >= '" + params.getFromDate() + "' \n";
        }

        if (params.getToDate() != null) {
            queryStr +="AND po.transaction_date <= '" + params.getToDate() + "' \n";
        }

        return queryStr;
    }

    public Integer getPurchaseOrderListCount(PurchaseOrderQ params) {

        try {
            String queryStr = "SELECT CAST(COUNT(1) AS INTEGER) AS totalCount\n";

            queryStr += getPurchaseOrderListQueryBody(params);

            Session session = em.unwrap(Session.class);

            NativeQuery query = session.createNativeQuery(queryStr);
            Object count = query.uniqueResult();

            if (count == null) {
                return 0;
            }

            return Integer.parseInt(count.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    @Transactional
    public String saveOrUpdatePurchaseOrder(PurchaseOrderSaveDto dto) throws UserInputValidationException {

        // TODO: role check (ADMIN only)

        Session session = em.unwrap(Session.class);
        PurchaseOrder purchaseOrder;
        validatePurchaseOrder(dto);

        if (dto.getPurchaseOrderId() != null && dto.getPurchaseOrderId() > 0) {

            Optional<PurchaseOrder> existingOpt = purchaseOrderRepository.findById(dto.getPurchaseOrderId());

            if (existingOpt.isEmpty()) {
                throw new UserInputValidationException("Purchase order not found with id: " + dto.getPurchaseOrderId());
            }

            purchaseOrder = existingOpt.get();

            if (!purchaseOrder.getPoNumber().equalsIgnoreCase(dto.getPoNumber().trim())
                    && purchaseOrderRepository.existsByPoNumberIgnoreCaseAndIdNot(dto.getPoNumber().trim(), dto.getPurchaseOrderId())) {
                throw new UserInputValidationException("A purchase order with this PO number already exists: " + dto.getDocumentNumber());
            }

            // Remove existing lines and recreate them.
            purchaseOrderPerLineRepository.deleteByPurchaseOrderId(purchaseOrder.getId());

        } else {

            if (purchaseOrderRepository.existsByPoNumberIgnoreCase(dto.getPoNumber().trim())) {
                throw new UserInputValidationException("A purchase order with this PO number already exists: " + dto.getDocumentNumber());
            }

            purchaseOrder = new PurchaseOrder();
            purchaseOrder.setCreatedAt(LocalDateTime.now());
            purchaseOrder.setDocumentNumber(generateDocumentNumber());
        }

        purchaseOrder.setPoNumber(dto.getPoNumber());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        purchaseOrder.setTransactionDate(LocalDate.parse(dto.getTransactionDate(), formatter));

        purchaseOrder.setNote(dto.getNote());
        purchaseOrder.setOverallStatus(dto.getOverallStatus());

        if (dto.getCreatedBy() != null) {
            purchaseOrder.setCreatedBy(dto.getCreatedBy());
        } else {
            purchaseOrder.setCreatedBy(1l);

        }

        /*
         * Calculate totals from order lines.
         */
        int totalQuantity = 0;
        Double totalAmount = 0.0;

        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);

        for (PurchaseOrderPerlineDto lineDto : dto.getLines()) {

            Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new UserInputValidationException("Product not found with id: " + lineDto.getProductId()));

            PurchaseOrderPerline line = new PurchaseOrderPerline();
            line.setPurchaseOrderId(savedOrder.getId());
            line.setProductId(product.getId());
            line.setQuantity(lineDto.getQuantity());
            line.setUnitPrice(lineDto.getUnitPrice());

            Double lineTotal = lineDto.getQuantity() * lineDto.getUnitPrice();

            line.setTotalAmount(lineTotal);

            totalQuantity += lineDto.getQuantity();
            totalAmount += lineTotal;

            purchaseOrderPerLineRepository.save(line);
        }

        savedOrder.setTotalQuantity(totalQuantity);
        savedOrder.setTotalAmount(totalAmount);
        purchaseOrderRepository.save(savedOrder);

        session.flush();

        if(savedOrder.getOverallStatus().equalsIgnoreCase("Completed")) {

            List<PurchaseOrderPerline> perlines = purchaseOrderPerLineRepository.findByPurchaseOrderId(savedOrder.getId());

            for(PurchaseOrderPerline perline : perlines) {

                Integer newBalance = inventoryRepository.increaseStock(perline.getProductId(), perline.getQuantity(), LocalDateTime.now()); // returns 30

                inventoryRepository.insertLedgerEntry(
                        perline.getProductId(), "STOCK_IN", perline.getQuantity(),
                        "PURCHASE_ORDER", savedOrder.getId(), savedOrder.getDocumentNumber(),
                        savedOrder.getTransactionDate(), newBalance, 1l, ""); // 30 goes in as balanceAfter
            }

        }


        return "Purchase order saved successfully";
    }

    public String generateDocumentNumber() {

        LocalDate today = LocalDate.now();

        String month = String.format("%02d", today.getMonthValue());
        String year = String.format("%02d", today.getYear() % 100);

        String prefix = "DO" + month + year;

        String lastDocumentNumber = purchaseOrderRepository.findLastDocumentNumberByPrefix(prefix);

        int sequence = 1;

        if (lastDocumentNumber != null) {
            String sequencePart = lastDocumentNumber.substring(prefix.length());

            sequence = Integer.parseInt(sequencePart) + 1;
        }

        return prefix + String.format("%05d", sequence);
    }

    private void validatePurchaseOrder(PurchaseOrderSaveDto dto) throws UserInputValidationException {

        if (dto.getPoNumber() == null || dto.getPoNumber().trim().isEmpty()) {
            throw new UserInputValidationException("PO number cannot be empty");
        }

        if (dto.getTransactionDate() == null) {
            throw new UserInputValidationException("Transaction date cannot be empty");
        }

        if (dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new UserInputValidationException("Purchase order must contain at least one product");
        }

        for (PurchaseOrderPerlineDto line : dto.getLines()) {

            if (line.getProductId() == null) {
                throw new UserInputValidationException("Product is required");
            }

            if (line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new UserInputValidationException("Quantity must be greater than zero");
            }

            if (line.getUnitPrice() == null || line.getUnitPrice() < 0) {
                throw new UserInputValidationException("Unit price cannot be negative");
            }
        }
    }


    public PurchaseOrderDto getPurchaseOrderById(Long id) throws UserInputValidationException {

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                        .orElseThrow(() -> new UserInputValidationException("Purchase order not found with id: " + id));

        List<PurchaseOrderPerline> lines = purchaseOrderPerLineRepository.findByPurchaseOrderId(id);

        PurchaseOrderDto dto = new PurchaseOrderDto();

        dto.setPurchaseOrderId(purchaseOrder.getId());
        dto.setDocumentNumber(purchaseOrder.getDocumentNumber());
        dto.setPoNumber(purchaseOrder.getPoNumber());
        dto.setTransactionDate(LocalDate.from(purchaseOrder.getTransactionDate()));
        dto.setNote(purchaseOrder.getNote());
        dto.setTotalQuantity(purchaseOrder.getTotalQuantity());
        dto.setTotalAmount(purchaseOrder.getTotalAmount());
        dto.setCreatedBy(purchaseOrder.getCreatedBy());
        dto.setCreatedAt(purchaseOrder.getCreatedAt());

        List<PurchaseOrderPerlineDto> lineDtos = new ArrayList<>();

        for (PurchaseOrderPerline line : lines) {
            PurchaseOrderPerlineDto lineDto = new PurchaseOrderPerlineDto();
            lineDto.setId(line.getId());
            lineDto.setProductId(line.getProductId());
            lineDto.setQuantity(line.getQuantity());
            lineDto.setUnitPrice(line.getUnitPrice());
            lineDtos.add(lineDto);
        }

        dto.setPerlines(lineDtos);

        return dto;
    }

    @Transactional
    public void deletePurchaseOrder(Long id) throws UserInputValidationException {

        // TODO: role check (ADMIN only)

        if (purchaseOrderRepository.findById(id).isEmpty()) {
            throw new UserInputValidationException("Purchase order not found with id: " + id);
        }

        purchaseOrderPerLineRepository.deleteByPurchaseOrderId(id);
        purchaseOrderRepository.deleteById(id);
    }
}
