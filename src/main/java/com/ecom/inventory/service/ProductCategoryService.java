package com.ecom.inventory.service;

import com.ecom.inventory.dao.Dao;
import com.ecom.inventory.dto.product.ProductCategorySaveDto;
import com.ecom.inventory.dto.product.ProductCategoryStatusDto;
import com.ecom.inventory.entity.ProductCategory;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.repository.ProductCategoryRepository;
import com.ecom.inventory.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductCategoryService extends Dao {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository; // usage check on delete

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

    public List<ProductCategory> getCategoryList() {
        return productCategoryRepository.findAllByOrderByCategoryNameAsc();
    }

    public ProductCategory getCategoryById(Long id) throws UserInputValidationException {
        return productCategoryRepository.findById(id)
                .orElseThrow(() -> new UserInputValidationException("Category not found with id: " + id));
    }

    @Transactional
    public String updateCategoryStatus(ProductCategoryStatusDto dto) throws UserInputValidationException {
        // TODO: role check (ADMIN only)
        if (dto.getActive() == null) {
            throw new UserInputValidationException("active flag is required");
        }
        ProductCategory category = productCategoryRepository.findById(dto.getId())
                .orElseThrow(() -> new UserInputValidationException("Category not found with id: " + dto.getId()));
        category.setActive(dto.getActive());
        productCategoryRepository.save(category);
        return dto.getActive() ? "Category activated successfully." : "Category deactivated successfully.";
    }

    @Transactional
    public void deleteCategory(Long id) throws UserInputValidationException {
        // TODO: role check (ADMIN only)
        if (productCategoryRepository.findById(id).isEmpty()) {
            throw new UserInputValidationException("Category not found with id: " + id);
        }

        long usageCount = productRepository.countByCategoryId(id);
        if (usageCount > 0) {
            throw new UserInputValidationException(
                    "This category is used by " + usageCount + " product(s). Deactivate it instead.");
        }

        productCategoryRepository.deleteById(id);
    }
}