package com.ecom.inventory.repository;

import com.ecom.inventory.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    boolean existsByCategoryNameIgnoreCase(String categoryName);
    boolean existsByCategoryNameIgnoreCaseAndIdNot(String categoryName, Long id);
    List<ProductCategory> findAllByOrderByCategoryNameAsc();
}
