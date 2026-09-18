package com.ecom.inventory.service;

import com.ecom.inventory.dto.common.PaginationDto;
import com.ecom.inventory.dto.product.*;
import com.ecom.inventory.entity.Product;
import com.ecom.inventory.entity.ProductCategory;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.repository.InventoryLedgerReadRepository;
import com.ecom.inventory.repository.ProductCategoryRepository;
import com.ecom.inventory.repository.ProductRepository;
import com.ecom.inventory.util.db.DtoResultTransformer;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final InventoryLedgerReadRepository inventoryLedgerReadRepository; // usage check on delete

    private final EntityManager em;

    @Transactional
    public String saveOrUpdateCategory(ProductCategorySaveDto dto) throws UserInputValidationException {
        // TODO: role check (ADMIN only)

        if (dto.getCategoryName() == null || dto.getCategoryName().trim().isEmpty()) {
            throw new UserInputValidationException("Category name cannot be empty");
        }

        ProductCategory category;

        if (dto.getId() != null && dto.getId() > 0) {
            Optional<ProductCategory> existingOpt = productCategoryRepository.findById(dto.getId());
            if (existingOpt.isEmpty()) {
                throw new UserInputValidationException("Category not found with id: " + dto.getId());
            }
            category = existingOpt.get();

            if (!category.getCategoryName().equalsIgnoreCase(dto.getCategoryName().trim())
                    && productCategoryRepository.existsByCategoryNameIgnoreCaseAndIdNot(
                    dto.getCategoryName().trim(), dto.getId())) {
                throw new UserInputValidationException(
                        "A category with this name already exists: " + dto.getCategoryName());
            }
        } else {
            if (productCategoryRepository.existsByCategoryNameIgnoreCase(dto.getCategoryName().trim())) {
                throw new UserInputValidationException(
                        "A category with this name already exists: " + dto.getCategoryName());
            }
            category = new ProductCategory();
            category.setCreatedAt(LocalDateTime.now());
        }

        category.setCategoryName(dto.getCategoryName().trim());
        category.setDescription(dto.getDescription());
        category.setActive(dto.getActive() != null ? dto.getActive() : true);
        category.setUpdatedAt(LocalDateTime.now());

        productCategoryRepository.save(category);
        return "Category saved successfully";
    }

    public PaginationDto getProductList(ProductQ params) throws UserInputValidationException, Exception {
        Session session = em.unwrap(Session.class);
        validateGetProductList(params);

        PaginationDto ret = new PaginationDto();

        try {
            String queryStr = "SELECT p.id productId,\n" +
                    "p.sku sku,\n" +
                    "p.product_name productName,\n" +
                    "p.category_id categoryId,\n" +
                    "pc.category_name productCategoryName,\n" +
                    "p.price price,\n" +
                    "p.pack_quantity packQuantity,\n" +
                    "p.pack_unit packUnit,\n" +
                    "p.min_stock_level minStockLevel,\n" +
                    "p.active active,\n" +
                    "p.created_at createdAt,\n" +
                    "p.updated_at updatedAt\n";

            queryStr += getProductListQueryBody(params);
            queryStr += "ORDER BY pc.category_name, p.product_name ASC LIMIT " + params.getLimit() + " OFFSET " + params.getStart();

            Integer totalRecords = getProductListCount(params);

            List<ProductDto> list = session.createNativeQuery(queryStr)
                    .setResultTransformer(new DtoResultTransformer(ProductDto.class))
                    .list();

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

    private String getProductListQueryBody(ProductQ params) {
        String queryStr = "FROM product p\n" +
                "JOIN product_category pc on pc.id = p.category_id\n" +
                "WHERE 1=1 \n";

        if (params.getKeyword() != null && !params.getKeyword().trim().isEmpty()) {
            String kw = params.getKeyword().trim();
            queryStr += "AND (p.product_name ILIKE '%" + kw + "%' OR p.sku ILIKE '%" + kw + "%') \n";
        }
        if (params.getCategoryId() != null) {
            queryStr += "AND p.category_id = " + params.getCategoryId() + " \n";
        }
        if (params.getActive() != null) {
            queryStr += "AND p.active = " + params.getActive() + " \n";
        }
        if (params.getMinPrice() != null) {
            queryStr += "AND p.price >= " + params.getMinPrice() + " \n";
        }
        if (params.getMaxPrice() != null) {
            queryStr += "AND p.price <= " + params.getMaxPrice() + " \n";
        }

        return queryStr;
    }

    public Integer getProductListCount(ProductQ params) {
        try {
            String queryStr = "SELECT CAST(COUNT(1) AS INTEGER) AS totalCount\n";
            queryStr += getProductListQueryBody(params);

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

    private void validateGetProductList(ProductQ params) throws UserInputValidationException {
        UserInputValidationException e = new UserInputValidationException();
        if (params.getStart() == null) {
            e.addErrorMessage("Product", "Start can't be empty");
        }
        if (params.getLimit() == null) {
            e.addErrorMessage("Product", "Limit can't be empty");
        }
        if (e.isValidationErrorOccured()) {
            throw e;
        }
    }

    public Page<Product> getProductList(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        String kw = keyword.trim();
        return productRepository.findByProductNameContainingIgnoreCaseOrSkuContainingIgnoreCase(kw, kw, pageable);
    }



    public ProductCategory getCategoryById(Long id) throws UserInputValidationException {
        return productCategoryRepository.findById(id)
                .orElseThrow(() -> new UserInputValidationException("Category not found with id: " + id));
    }

    @Transactional
    public String saveOrUpdateProduct(ProductSaveDto dto) throws UserInputValidationException {
        // TODO: role check (ADMIN only)

        if (dto.getSku() == null || dto.getSku().trim().isEmpty()) {
            throw new UserInputValidationException("SKU cannot be empty");
        } // sku cant be same, unique
        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new UserInputValidationException("Product name cannot be empty");
        }
        if (dto.getPrice() == null) {
            throw new UserInputValidationException("Price cannot be empty");
        }
        if (dto.getCategoryId() != null && productCategoryRepository.findById(dto.getCategoryId()).isEmpty()) {
            throw new UserInputValidationException("Category not found with id: " + dto.getCategoryId());
        }

        Product product;

        if (dto.getId() != null && dto.getId() > 0) {
            Optional<Product> existingOpt = productRepository.findById(dto.getId());
            if (existingOpt.isEmpty()) {
                throw new UserInputValidationException("Product not found with id: " + dto.getId());
            }
            product = existingOpt.get();

            if (!product.getSku().equalsIgnoreCase(dto.getSku().trim())
                    && productRepository.existsBySkuIgnoreCaseAndIdNot(dto.getSku().trim(), dto.getId())) {
                throw new UserInputValidationException("A product with this SKU already exists: " + dto.getSku());
            }
        } else {
            if (productRepository.existsBySkuIgnoreCase(dto.getSku().trim())) {
                throw new UserInputValidationException("A product with this SKU already exists: " + dto.getSku());
            }
            product = new Product();
            product.setCreatedAt(LocalDateTime.now());
        }

        product.setSku(dto.getSku().trim());
        product.setProductName(dto.getProductName().trim());
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setPackQuantity(dto.getPackQuantity());
        product.setPackUnit(dto.getPackUnit());
        product.setMinStockLevel(dto.getMinStockLevel() != null ? dto.getMinStockLevel() : 0);
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);
        return "Product saved successfully";
    }

    public Product getProductById(Long id) throws UserInputValidationException {
        return productRepository.findById(id)
                .orElseThrow(() -> new UserInputValidationException("Product not found with id: " + id));
    }

    @Transactional
    public String updateProductStatus(ProductStatusDto dto) throws UserInputValidationException {
        // TODO: role check (ADMIN only)
        if (dto.getActive() == null) {
            throw new UserInputValidationException("active flag is required");
        }
        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new UserInputValidationException("Product not found with id: " + dto.getId()));
        product.setActive(dto.getActive());
        productRepository.save(product);
        return dto.getActive() ? "Product activated successfully." : "Product deactivated successfully.";
    }

    @Transactional
    public void deleteProduct(Long id) throws UserInputValidationException {
        // TODO: role check (ADMIN only)
        if (productRepository.findById(id).isEmpty()) {
            throw new UserInputValidationException("Product not found with id: " + id);
        }

        // inventory_ledger is the source of truth for every stock movement — one check covers it.
        long usageCount = inventoryLedgerReadRepository.countByProductId(id);
        if (usageCount > 0) {
            throw new UserInputValidationException(
                    "This product has " + usageCount + " stock transaction(s). Deactivate it instead.");
        }

        productRepository.deleteById(id);
    }
}
