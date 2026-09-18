package com.ecom.inventory.repository;

import com.ecom.inventory.entity.Product;
import com.ecom.inventory.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);
    long countByCategoryId(Long categoryId);
    // same keyword passed twice from the service — matches against name OR sku
    Page<Product> findByProductNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String productNameKeyword, String skuKeyword, Pageable pageable);

}
